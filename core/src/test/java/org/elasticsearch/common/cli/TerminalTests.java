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
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|NoSuchFileException
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
name|hasSize
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TerminalTests
specifier|public
class|class
name|TerminalTests
extends|extends
name|CliToolTestCase
block|{
DECL|method|testVerbosity
specifier|public
name|void
name|testVerbosity
parameter_list|()
throws|throws
name|Exception
block|{
name|CaptureOutputTerminal
name|terminal
init|=
operator|new
name|CaptureOutputTerminal
argument_list|(
name|Terminal
operator|.
name|Verbosity
operator|.
name|SILENT
argument_list|)
decl_stmt|;
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
name|CaptureOutputTerminal
argument_list|(
name|Terminal
operator|.
name|Verbosity
operator|.
name|NORMAL
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
name|CaptureOutputTerminal
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
DECL|method|testError
specifier|public
name|void
name|testError
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
comment|// actually throw so we have a stacktrace
throw|throw
operator|new
name|NoSuchFileException
argument_list|(
literal|"/path/to/some/file"
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|NoSuchFileException
name|e
parameter_list|)
block|{
name|CaptureOutputTerminal
name|terminal
init|=
operator|new
name|CaptureOutputTerminal
argument_list|(
name|Terminal
operator|.
name|Verbosity
operator|.
name|NORMAL
argument_list|)
decl_stmt|;
name|terminal
operator|.
name|printError
argument_list|(
name|e
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|output
init|=
name|terminal
operator|.
name|getTerminalOutput
argument_list|()
decl_stmt|;
name|assertFalse
argument_list|(
name|output
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|output
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|output
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|contains
argument_list|(
literal|"NoSuchFileException"
argument_list|)
argument_list|)
expr_stmt|;
comment|// exception class
name|assertTrue
argument_list|(
name|output
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|output
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|contains
argument_list|(
literal|"/path/to/some/file"
argument_list|)
argument_list|)
expr_stmt|;
comment|// message
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|output
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO: we should test stack trace is printed in debug mode...except debug is a sysprop instead of
comment|// a command line param...maybe it should be VERBOSE instead of a separate debug prop?
block|}
block|}
DECL|method|assertPrinted
specifier|private
name|void
name|assertPrinted
parameter_list|(
name|CaptureOutputTerminal
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
block|{
name|logTerminal
operator|.
name|print
argument_list|(
name|verbosity
argument_list|,
name|text
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|logTerminal
operator|.
name|getTerminalOutput
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|logTerminal
operator|.
name|getTerminalOutput
argument_list|()
argument_list|,
name|hasItem
argument_list|(
name|text
argument_list|)
argument_list|)
expr_stmt|;
name|logTerminal
operator|.
name|terminalOutput
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
DECL|method|assertNotPrinted
specifier|private
name|void
name|assertNotPrinted
parameter_list|(
name|CaptureOutputTerminal
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
block|{
name|logTerminal
operator|.
name|print
argument_list|(
name|verbosity
argument_list|,
name|text
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|logTerminal
operator|.
name|getTerminalOutput
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

