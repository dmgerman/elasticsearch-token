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
name|ExceptionsHelper
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
name|SuppressForbidden
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

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|StreamsUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|PrintWriter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Writer
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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
name|greaterThan
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

begin_class
DECL|class|CliToolTestCase
specifier|public
specifier|abstract
class|class
name|CliToolTestCase
extends|extends
name|ESTestCase
block|{
annotation|@
name|Before
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"sets es.default.path.home during tests"
argument_list|)
DECL|method|setPathHome
specifier|public
name|void
name|setPathHome
parameter_list|()
block|{
name|System
operator|.
name|setProperty
argument_list|(
literal|"es.default.path.home"
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"clears es.default.path.home during tests"
argument_list|)
DECL|method|clearPathHome
specifier|public
name|void
name|clearPathHome
parameter_list|()
block|{
name|System
operator|.
name|clearProperty
argument_list|(
literal|"es.default.path.home"
argument_list|)
expr_stmt|;
block|}
DECL|method|args
specifier|public
specifier|static
name|String
index|[]
name|args
parameter_list|(
name|String
name|command
parameter_list|)
block|{
if|if
condition|(
operator|!
name|Strings
operator|.
name|hasLength
argument_list|(
name|command
argument_list|)
condition|)
block|{
return|return
name|Strings
operator|.
name|EMPTY_ARRAY
return|;
block|}
return|return
name|command
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|)
return|;
block|}
comment|/**      * A terminal implementation that discards everything      */
DECL|class|MockTerminal
specifier|public
specifier|static
class|class
name|MockTerminal
extends|extends
name|Terminal
block|{
DECL|field|DEV_NULL
specifier|private
specifier|static
specifier|final
name|PrintWriter
name|DEV_NULL
init|=
operator|new
name|PrintWriter
argument_list|(
operator|new
name|DevNullWriter
argument_list|()
argument_list|)
decl_stmt|;
DECL|method|MockTerminal
specifier|public
name|MockTerminal
parameter_list|()
block|{
name|super
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|)
expr_stmt|;
block|}
DECL|method|MockTerminal
specifier|public
name|MockTerminal
parameter_list|(
name|Verbosity
name|verbosity
parameter_list|)
block|{
name|super
argument_list|(
name|verbosity
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doPrint
specifier|protected
name|void
name|doPrint
parameter_list|(
name|String
name|msg
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{         }
annotation|@
name|Override
DECL|method|readText
specifier|public
name|String
name|readText
parameter_list|(
name|String
name|text
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|readSecret
specifier|public
name|char
index|[]
name|readSecret
parameter_list|(
name|String
name|text
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
return|return
operator|new
name|char
index|[
literal|0
index|]
return|;
block|}
annotation|@
name|Override
DECL|method|print
specifier|public
name|void
name|print
parameter_list|(
name|String
name|msg
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{         }
annotation|@
name|Override
DECL|method|printStackTrace
specifier|public
name|void
name|printStackTrace
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
return|return;
block|}
annotation|@
name|Override
DECL|method|writer
specifier|public
name|PrintWriter
name|writer
parameter_list|()
block|{
return|return
name|DEV_NULL
return|;
block|}
DECL|class|DevNullWriter
specifier|private
specifier|static
class|class
name|DevNullWriter
extends|extends
name|Writer
block|{
annotation|@
name|Override
DECL|method|write
specifier|public
name|void
name|write
parameter_list|(
name|char
index|[]
name|cbuf
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{             }
annotation|@
name|Override
DECL|method|flush
specifier|public
name|void
name|flush
parameter_list|()
throws|throws
name|IOException
block|{             }
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{             }
block|}
block|}
comment|/**      * A terminal implementation that captures everything written to it      */
DECL|class|CaptureOutputTerminal
specifier|public
specifier|static
class|class
name|CaptureOutputTerminal
extends|extends
name|MockTerminal
block|{
DECL|field|terminalOutput
name|List
argument_list|<
name|String
argument_list|>
name|terminalOutput
init|=
operator|new
name|ArrayList
argument_list|()
decl_stmt|;
DECL|method|CaptureOutputTerminal
specifier|public
name|CaptureOutputTerminal
parameter_list|()
block|{
name|super
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|)
expr_stmt|;
block|}
DECL|method|CaptureOutputTerminal
specifier|public
name|CaptureOutputTerminal
parameter_list|(
name|Verbosity
name|verbosity
parameter_list|)
block|{
name|super
argument_list|(
name|verbosity
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doPrint
specifier|protected
name|void
name|doPrint
parameter_list|(
name|String
name|msg
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|terminalOutput
operator|.
name|add
argument_list|(
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
name|msg
argument_list|,
name|args
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|print
specifier|public
name|void
name|print
parameter_list|(
name|String
name|msg
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|doPrint
argument_list|(
name|msg
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|printStackTrace
specifier|public
name|void
name|printStackTrace
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|terminalOutput
operator|.
name|add
argument_list|(
name|ExceptionsHelper
operator|.
name|stackTrace
argument_list|(
name|t
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getTerminalOutput
specifier|public
name|List
argument_list|<
name|String
argument_list|>
name|getTerminalOutput
parameter_list|()
block|{
return|return
name|terminalOutput
return|;
block|}
block|}
DECL|method|assertTerminalOutputContainsHelpFile
specifier|public
specifier|static
name|void
name|assertTerminalOutputContainsHelpFile
parameter_list|(
name|CliToolTestCase
operator|.
name|CaptureOutputTerminal
name|terminal
parameter_list|,
name|String
name|classPath
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|String
argument_list|>
name|nonEmptyLines
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|line
range|:
name|terminal
operator|.
name|getTerminalOutput
argument_list|()
control|)
block|{
name|String
name|originalPrintedLine
init|=
name|line
operator|.
name|replaceAll
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|,
literal|""
argument_list|)
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|isNullOrEmpty
argument_list|(
name|originalPrintedLine
argument_list|)
condition|)
block|{
name|nonEmptyLines
operator|.
name|add
argument_list|(
name|originalPrintedLine
argument_list|)
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|nonEmptyLines
argument_list|,
name|hasSize
argument_list|(
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|expectedDocs
init|=
name|StreamsUtils
operator|.
name|copyToStringFromClasspath
argument_list|(
name|classPath
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|nonEmptyLine
range|:
name|nonEmptyLines
control|)
block|{
name|assertThat
argument_list|(
name|expectedDocs
argument_list|,
name|containsString
argument_list|(
name|nonEmptyLine
operator|.
name|replaceAll
argument_list|(
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|,
literal|""
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

