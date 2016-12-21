begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugins
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
package|;
end_package

begin_import
import|import
name|joptsimple
operator|.
name|OptionSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|EnvironmentAwareCommand
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
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
name|node
operator|.
name|internal
operator|.
name|InternalSettingsPreparer
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
name|nio
operator|.
name|file
operator|.
name|DirectoryStream
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
name|Map
import|;
end_import

begin_comment
comment|/**  * A command for the plugin cli to list plugins installed in elasticsearch.  */
end_comment

begin_class
DECL|class|ListPluginsCommand
class|class
name|ListPluginsCommand
extends|extends
name|EnvironmentAwareCommand
block|{
DECL|method|ListPluginsCommand
name|ListPluginsCommand
parameter_list|()
block|{
name|super
argument_list|(
literal|"Lists installed elasticsearch plugins"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|protected
name|void
name|execute
parameter_list|(
name|Terminal
name|terminal
parameter_list|,
name|OptionSet
name|options
parameter_list|,
name|Environment
name|env
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|Files
operator|.
name|exists
argument_list|(
name|env
operator|.
name|pluginsFile
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Plugins directory missing: "
operator|+
name|env
operator|.
name|pluginsFile
argument_list|()
argument_list|)
throw|;
block|}
name|terminal
operator|.
name|println
argument_list|(
name|Terminal
operator|.
name|Verbosity
operator|.
name|VERBOSE
argument_list|,
literal|"Plugins directory: "
operator|+
name|env
operator|.
name|pluginsFile
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|List
argument_list|<
name|Path
argument_list|>
name|plugins
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|DirectoryStream
argument_list|<
name|Path
argument_list|>
name|paths
init|=
name|Files
operator|.
name|newDirectoryStream
argument_list|(
name|env
operator|.
name|pluginsFile
argument_list|()
argument_list|)
init|)
block|{
for|for
control|(
name|Path
name|plugin
range|:
name|paths
control|)
block|{
name|plugins
operator|.
name|add
argument_list|(
name|plugin
argument_list|)
expr_stmt|;
block|}
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|plugins
argument_list|)
expr_stmt|;
for|for
control|(
specifier|final
name|Path
name|plugin
range|:
name|plugins
control|)
block|{
name|terminal
operator|.
name|println
argument_list|(
name|plugin
operator|.
name|getFileName
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|PluginInfo
name|info
init|=
name|PluginInfo
operator|.
name|readFromProperties
argument_list|(
name|env
operator|.
name|pluginsFile
argument_list|()
operator|.
name|resolve
argument_list|(
name|plugin
operator|.
name|toAbsolutePath
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|terminal
operator|.
name|println
argument_list|(
name|Terminal
operator|.
name|Verbosity
operator|.
name|VERBOSE
argument_list|,
name|info
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

