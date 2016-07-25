begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cli
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cli
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
name|junit
operator|.
name|Before
import|;
end_import

begin_class
DECL|class|MultiCommandTests
specifier|public
class|class
name|MultiCommandTests
extends|extends
name|CommandTestCase
block|{
DECL|class|DummyMultiCommand
specifier|static
class|class
name|DummyMultiCommand
extends|extends
name|MultiCommand
block|{
DECL|method|DummyMultiCommand
name|DummyMultiCommand
parameter_list|()
block|{
name|super
argument_list|(
literal|"A dummy multi command"
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|DummySubCommand
specifier|static
class|class
name|DummySubCommand
extends|extends
name|Command
block|{
DECL|method|DummySubCommand
name|DummySubCommand
parameter_list|()
block|{
name|super
argument_list|(
literal|"A dummy subcommand"
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
parameter_list|)
throws|throws
name|Exception
block|{
name|terminal
operator|.
name|println
argument_list|(
literal|"Arguments: "
operator|+
name|options
operator|.
name|nonOptionArguments
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|field|multiCommand
name|DummyMultiCommand
name|multiCommand
decl_stmt|;
annotation|@
name|Before
DECL|method|setupCommand
specifier|public
name|void
name|setupCommand
parameter_list|()
block|{
name|multiCommand
operator|=
operator|new
name|DummyMultiCommand
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|newCommand
specifier|protected
name|Command
name|newCommand
parameter_list|()
block|{
return|return
name|multiCommand
return|;
block|}
DECL|method|testNoCommandsConfigured
specifier|public
name|void
name|testNoCommandsConfigured
parameter_list|()
throws|throws
name|Exception
block|{
name|IllegalStateException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|execute
argument_list|()
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"No subcommands configured"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testUnknownCommand
specifier|public
name|void
name|testUnknownCommand
parameter_list|()
throws|throws
name|Exception
block|{
name|multiCommand
operator|.
name|subcommands
operator|.
name|put
argument_list|(
literal|"something"
argument_list|,
operator|new
name|DummySubCommand
argument_list|()
argument_list|)
expr_stmt|;
name|UserException
name|e
init|=
name|expectThrows
argument_list|(
name|UserException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|execute
argument_list|(
literal|"somethingelse"
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ExitCodes
operator|.
name|USAGE
argument_list|,
name|e
operator|.
name|exitCode
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Unknown command [somethingelse]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testMissingCommand
specifier|public
name|void
name|testMissingCommand
parameter_list|()
throws|throws
name|Exception
block|{
name|multiCommand
operator|.
name|subcommands
operator|.
name|put
argument_list|(
literal|"command1"
argument_list|,
operator|new
name|DummySubCommand
argument_list|()
argument_list|)
expr_stmt|;
name|UserException
name|e
init|=
name|expectThrows
argument_list|(
name|UserException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|execute
argument_list|()
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|ExitCodes
operator|.
name|USAGE
argument_list|,
name|e
operator|.
name|exitCode
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Missing command"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testHelp
specifier|public
name|void
name|testHelp
parameter_list|()
throws|throws
name|Exception
block|{
name|multiCommand
operator|.
name|subcommands
operator|.
name|put
argument_list|(
literal|"command1"
argument_list|,
operator|new
name|DummySubCommand
argument_list|()
argument_list|)
expr_stmt|;
name|multiCommand
operator|.
name|subcommands
operator|.
name|put
argument_list|(
literal|"command2"
argument_list|,
operator|new
name|DummySubCommand
argument_list|()
argument_list|)
expr_stmt|;
name|execute
argument_list|(
literal|"-h"
argument_list|)
expr_stmt|;
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
literal|"command1"
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
literal|"command2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSubcommandHelp
specifier|public
name|void
name|testSubcommandHelp
parameter_list|()
throws|throws
name|Exception
block|{
name|multiCommand
operator|.
name|subcommands
operator|.
name|put
argument_list|(
literal|"command1"
argument_list|,
operator|new
name|DummySubCommand
argument_list|()
argument_list|)
expr_stmt|;
name|multiCommand
operator|.
name|subcommands
operator|.
name|put
argument_list|(
literal|"command2"
argument_list|,
operator|new
name|DummySubCommand
argument_list|()
argument_list|)
expr_stmt|;
name|execute
argument_list|(
literal|"command2"
argument_list|,
literal|"-h"
argument_list|)
expr_stmt|;
name|String
name|output
init|=
name|terminal
operator|.
name|getOutput
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|output
argument_list|,
name|output
operator|.
name|contains
argument_list|(
literal|"command1"
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
literal|"A dummy subcommand"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSubcommandArguments
specifier|public
name|void
name|testSubcommandArguments
parameter_list|()
throws|throws
name|Exception
block|{
name|multiCommand
operator|.
name|subcommands
operator|.
name|put
argument_list|(
literal|"command1"
argument_list|,
operator|new
name|DummySubCommand
argument_list|()
argument_list|)
expr_stmt|;
name|execute
argument_list|(
literal|"command1"
argument_list|,
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|String
name|output
init|=
name|terminal
operator|.
name|getOutput
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|output
argument_list|,
name|output
operator|.
name|contains
argument_list|(
literal|"command1"
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
literal|"Arguments: [foo, bar]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

