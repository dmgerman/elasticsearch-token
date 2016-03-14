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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|LuceneTestCase
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
name|ExitCodes
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
name|MockTerminal
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
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_class
annotation|@
name|LuceneTestCase
operator|.
name|SuppressFileSystems
argument_list|(
literal|"*"
argument_list|)
DECL|class|ListPluginsCommandTests
specifier|public
class|class
name|ListPluginsCommandTests
extends|extends
name|ESTestCase
block|{
DECL|method|createEnv
name|Environment
name|createEnv
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|home
init|=
name|createTempDir
argument_list|()
decl_stmt|;
name|Files
operator|.
name|createDirectories
argument_list|(
name|home
operator|.
name|resolve
argument_list|(
literal|"plugins"
argument_list|)
argument_list|)
expr_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"path.home"
argument_list|,
name|home
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
return|;
block|}
DECL|method|listPlugins
specifier|static
name|MockTerminal
name|listPlugins
parameter_list|(
name|Environment
name|env
parameter_list|)
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
name|String
index|[]
name|args
init|=
block|{}
decl_stmt|;
name|int
name|status
init|=
operator|new
name|ListPluginsCommand
argument_list|(
name|env
argument_list|)
operator|.
name|main
argument_list|(
name|args
argument_list|,
name|terminal
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ExitCodes
operator|.
name|OK
argument_list|,
name|status
argument_list|)
expr_stmt|;
return|return
name|terminal
return|;
block|}
DECL|method|testPluginsDirMissing
specifier|public
name|void
name|testPluginsDirMissing
parameter_list|()
throws|throws
name|Exception
block|{
name|Environment
name|env
init|=
name|createEnv
argument_list|()
decl_stmt|;
name|Files
operator|.
name|delete
argument_list|(
name|env
operator|.
name|pluginsFile
argument_list|()
argument_list|)
expr_stmt|;
name|IOException
name|e
init|=
name|expectThrows
argument_list|(
name|IOException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|listPlugins
argument_list|(
name|env
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"Plugins directory missing"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoPlugins
specifier|public
name|void
name|testNoPlugins
parameter_list|()
throws|throws
name|Exception
block|{
name|MockTerminal
name|terminal
init|=
name|listPlugins
argument_list|(
name|createEnv
argument_list|()
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|terminal
operator|.
name|getOutput
argument_list|()
argument_list|,
name|terminal
operator|.
name|getOutput
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testOnePlugin
specifier|public
name|void
name|testOnePlugin
parameter_list|()
throws|throws
name|Exception
block|{
name|Environment
name|env
init|=
name|createEnv
argument_list|()
decl_stmt|;
name|Files
operator|.
name|createDirectory
argument_list|(
name|env
operator|.
name|pluginsFile
argument_list|()
operator|.
name|resolve
argument_list|(
literal|"fake"
argument_list|)
argument_list|)
expr_stmt|;
name|MockTerminal
name|terminal
init|=
name|listPlugins
argument_list|(
name|env
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|terminal
operator|.
name|getOutput
argument_list|()
argument_list|,
name|terminal
operator|.
name|getOutput
argument_list|()
operator|.
name|contains
argument_list|(
literal|"fake"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testTwoPlugins
specifier|public
name|void
name|testTwoPlugins
parameter_list|()
throws|throws
name|Exception
block|{
name|Environment
name|env
init|=
name|createEnv
argument_list|()
decl_stmt|;
name|Files
operator|.
name|createDirectory
argument_list|(
name|env
operator|.
name|pluginsFile
argument_list|()
operator|.
name|resolve
argument_list|(
literal|"fake1"
argument_list|)
argument_list|)
expr_stmt|;
name|Files
operator|.
name|createDirectory
argument_list|(
name|env
operator|.
name|pluginsFile
argument_list|()
operator|.
name|resolve
argument_list|(
literal|"fake2"
argument_list|)
argument_list|)
expr_stmt|;
name|MockTerminal
name|terminal
init|=
name|listPlugins
argument_list|(
name|env
argument_list|)
decl_stmt|;
name|String
name|output
init|=
name|terminal
operator|.
name|getOutput
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|output
argument_list|,
name|output
operator|.
name|contains
argument_list|(
literal|"fake1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|output
argument_list|,
name|output
operator|.
name|contains
argument_list|(
literal|"fake2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

