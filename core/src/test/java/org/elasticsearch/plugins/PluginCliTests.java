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
name|common
operator|.
name|cli
operator|.
name|CliToolTestCase
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
name|MockTerminal
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
name|cli
operator|.
name|CliTool
operator|.
name|ExitStatus
operator|.
name|OK_AND_EXIT
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|containsString
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|hasItem
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_class
DECL|class|PluginCliTests
specifier|public
class|class
name|PluginCliTests
extends|extends
name|CliToolTestCase
block|{
DECL|method|testHelpWorks
specifier|public
name|void
name|testHelpWorks
parameter_list|()
throws|throws
name|Exception
block|{
name|MockTerminal
name|terminal
init|=
operator|new
name|MockTerminal
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
operator|new
name|PluginCli
argument_list|(
name|terminal
argument_list|)
operator|.
name|execute
argument_list|(
name|args
argument_list|(
literal|"--help"
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
name|OK_AND_EXIT
argument_list|)
argument_list|)
expr_stmt|;
name|assertTerminalOutputContainsHelpFile
argument_list|(
name|terminal
argument_list|,
literal|"/org/elasticsearch/plugins/plugin.help"
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|resetOutput
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|PluginCli
argument_list|(
name|terminal
argument_list|)
operator|.
name|execute
argument_list|(
name|args
argument_list|(
literal|"install -h"
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
name|OK_AND_EXIT
argument_list|)
argument_list|)
expr_stmt|;
name|assertTerminalOutputContainsHelpFile
argument_list|(
name|terminal
argument_list|,
literal|"/org/elasticsearch/plugins/plugin-install.help"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|plugin
range|:
name|InstallPluginCommand
operator|.
name|OFFICIAL_PLUGINS
control|)
block|{
name|assertThat
argument_list|(
name|terminal
operator|.
name|getOutput
argument_list|()
argument_list|,
name|containsString
argument_list|(
name|plugin
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|terminal
operator|.
name|resetOutput
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|PluginCli
argument_list|(
name|terminal
argument_list|)
operator|.
name|execute
argument_list|(
name|args
argument_list|(
literal|"remove --help"
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
name|OK_AND_EXIT
argument_list|)
argument_list|)
expr_stmt|;
name|assertTerminalOutputContainsHelpFile
argument_list|(
name|terminal
argument_list|,
literal|"/org/elasticsearch/plugins/plugin-remove.help"
argument_list|)
expr_stmt|;
name|terminal
operator|.
name|resetOutput
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
operator|new
name|PluginCli
argument_list|(
name|terminal
argument_list|)
operator|.
name|execute
argument_list|(
name|args
argument_list|(
literal|"list -h"
argument_list|)
argument_list|)
argument_list|,
name|is
argument_list|(
name|OK_AND_EXIT
argument_list|)
argument_list|)
expr_stmt|;
name|assertTerminalOutputContainsHelpFile
argument_list|(
name|terminal
argument_list|,
literal|"/org/elasticsearch/plugins/plugin-list.help"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
