begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.cli
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|cli
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
name|MockTerminal
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
DECL|class|TerminalTests
specifier|public
class|class
name|TerminalTests
extends|extends
name|ESTestCase
block|{
DECL|method|testVerbosity
specifier|public
name|void
name|testVerbosity
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
name|terminal
operator|.
name|setVerbosity
argument_list|(
name|Terminal
operator|.
name|Verbosity
operator|.
name|SILENT
argument_list|)
expr_stmt|;
name|assertPrinted
argument_list|(
name|terminal
argument_list|,
name|Terminal
operator|.
name|Verbosity
operator|.
name|SILENT
argument_list|,
literal|"text"
argument_list|)
expr_stmt|;
name|assertNotPrinted
argument_list|(
name|terminal
argument_list|,
name|Terminal
operator|.
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"text"
argument_list|)
expr_stmt|;
name|assertNotPrinted
argument_list|(
name|terminal
argument_list|,
name|Terminal
operator|.
name|Verbosity
operator|.
name|VERBOSE
argument_list|,
literal|"text"
argument_list|)
expr_stmt|;
name|terminal
operator|=
operator|new
name|MockTerminal
argument_list|()
expr_stmt|;
name|assertPrinted
argument_list|(
name|terminal
argument_list|,
name|Terminal
operator|.
name|Verbosity
operator|.
name|SILENT
argument_list|,
literal|"text"
argument_list|)
expr_stmt|;
name|assertPrinted
argument_list|(
name|terminal
argument_list|,
name|Terminal
operator|.
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"text"
argument_list|)
expr_stmt|;
name|assertNotPrinted
argument_list|(
name|terminal
argument_list|,
name|Terminal
operator|.
name|Verbosity
operator|.
name|VERBOSE
argument_list|,
literal|"text"
argument_list|)
expr_stmt|;
name|terminal
operator|=
operator|new
name|MockTerminal
argument_list|()
expr_stmt|;
name|terminal
operator|.
name|setVerbosity
argument_list|(
name|Terminal
operator|.
name|Verbosity
operator|.
name|VERBOSE
argument_list|)
expr_stmt|;
name|assertPrinted
argument_list|(
name|terminal
argument_list|,
name|Terminal
operator|.
name|Verbosity
operator|.
name|SILENT
argument_list|,
literal|"text"
argument_list|)
expr_stmt|;
name|assertPrinted
argument_list|(
name|terminal
argument_list|,
name|Terminal
operator|.
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"text"
argument_list|)
expr_stmt|;
name|assertPrinted
argument_list|(
name|terminal
argument_list|,
name|Terminal
operator|.
name|Verbosity
operator|.
name|VERBOSE
argument_list|,
literal|"text"
argument_list|)
expr_stmt|;
block|}
DECL|method|testEscaping
specifier|public
name|void
name|testEscaping
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
name|assertPrinted
argument_list|(
name|terminal
argument_list|,
name|Terminal
operator|.
name|Verbosity
operator|.
name|NORMAL
argument_list|,
literal|"This message contains percent like %20n"
argument_list|)
expr_stmt|;
block|}
DECL|method|assertPrinted
specifier|private
name|void
name|assertPrinted
parameter_list|(
name|MockTerminal
name|logTerminal
parameter_list|,
name|Terminal
operator|.
name|Verbosity
name|verbosity
parameter_list|,
name|String
name|text
parameter_list|)
throws|throws
name|Exception
block|{
name|logTerminal
operator|.
name|println
argument_list|(
name|verbosity
argument_list|,
name|text
argument_list|)
expr_stmt|;
name|String
name|output
init|=
name|logTerminal
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
name|text
argument_list|)
argument_list|)
expr_stmt|;
name|logTerminal
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
DECL|method|assertNotPrinted
specifier|private
name|void
name|assertNotPrinted
parameter_list|(
name|MockTerminal
name|logTerminal
parameter_list|,
name|Terminal
operator|.
name|Verbosity
name|verbosity
parameter_list|,
name|String
name|text
parameter_list|)
throws|throws
name|Exception
block|{
name|logTerminal
operator|.
name|println
argument_list|(
name|verbosity
argument_list|,
name|text
argument_list|)
expr_stmt|;
name|String
name|output
init|=
name|logTerminal
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
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

