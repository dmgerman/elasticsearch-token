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
name|org
operator|.
name|elasticsearch
operator|.
name|cli
operator|.
name|MultiCommand
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
name|logging
operator|.
name|LogConfigurator
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
name|util
operator|.
name|Collections
import|;
end_import

begin_comment
comment|/**  * A cli tool for adding, removing and listing plugins for elasticsearch.  */
end_comment

begin_class
DECL|class|PluginCli
specifier|public
class|class
name|PluginCli
extends|extends
name|MultiCommand
block|{
DECL|method|PluginCli
specifier|public
name|PluginCli
parameter_list|()
block|{
name|super
argument_list|(
literal|"A tool for managing installed elasticsearch plugins"
argument_list|)
expr_stmt|;
name|subcommands
operator|.
name|put
argument_list|(
literal|"list"
argument_list|,
operator|new
name|ListPluginsCommand
argument_list|()
argument_list|)
expr_stmt|;
name|subcommands
operator|.
name|put
argument_list|(
literal|"install"
argument_list|,
operator|new
name|InstallPluginCommand
argument_list|()
argument_list|)
expr_stmt|;
name|subcommands
operator|.
name|put
argument_list|(
literal|"remove"
argument_list|,
operator|new
name|RemovePluginCommand
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
comment|// initialize default for es.logger.level because we will not read the logging.yml
name|String
name|loggerLevel
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"es.logger.level"
argument_list|,
literal|"INFO"
argument_list|)
decl_stmt|;
name|String
name|pathHome
init|=
name|System
operator|.
name|getProperty
argument_list|(
literal|"es.path.home"
argument_list|)
decl_stmt|;
comment|// Set the appender for all potential log files to terminal so that other components that use the logger print out the
comment|// same terminal.
comment|// The reason for this is that the plugin cli cannot be configured with a file appender because when the plugin command is
comment|// executed there is no way of knowing where the logfiles should be placed. For example, if elasticsearch
comment|// is run as service then the logs should be at /var/log/elasticsearch but when started from the tar they should be at es.home/logs.
comment|// Therefore we print to Terminal.
name|Environment
name|loggingEnvironment
init|=
name|InternalSettingsPreparer
operator|.
name|prepareEnvironment
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|pathHome
argument_list|)
operator|.
name|put
argument_list|(
literal|"appender.terminal.type"
argument_list|,
literal|"terminal"
argument_list|)
operator|.
name|put
argument_list|(
literal|"rootLogger"
argument_list|,
literal|"${logger.level}, terminal"
argument_list|)
operator|.
name|put
argument_list|(
literal|"logger.level"
argument_list|,
name|loggerLevel
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|Terminal
operator|.
name|DEFAULT
argument_list|)
decl_stmt|;
name|LogConfigurator
operator|.
name|configure
argument_list|(
name|loggingEnvironment
operator|.
name|settings
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|exit
argument_list|(
operator|new
name|PluginCli
argument_list|()
operator|.
name|main
argument_list|(
name|args
argument_list|,
name|Terminal
operator|.
name|DEFAULT
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

