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
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|CommandLine
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
name|Console
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
name|InputStreamReader
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
name|util
operator|.
name|Locale
import|;
end_import

begin_comment
comment|/** * */
end_comment

begin_class
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"System#out"
argument_list|)
DECL|class|Terminal
specifier|public
specifier|abstract
class|class
name|Terminal
block|{
DECL|field|DEBUG_SYSTEM_PROPERTY
specifier|public
specifier|static
specifier|final
name|String
name|DEBUG_SYSTEM_PROPERTY
init|=
literal|"es.cli.debug"
decl_stmt|;
DECL|field|DEFAULT
specifier|public
specifier|static
specifier|final
name|Terminal
name|DEFAULT
init|=
name|ConsoleTerminal
operator|.
name|supported
argument_list|()
condition|?
operator|new
name|ConsoleTerminal
argument_list|()
else|:
operator|new
name|SystemTerminal
argument_list|()
decl_stmt|;
DECL|enum|Verbosity
specifier|public
specifier|static
enum|enum
name|Verbosity
block|{
DECL|enum constant|SILENT
DECL|enum constant|NORMAL
DECL|enum constant|VERBOSE
name|SILENT
argument_list|(
literal|0
argument_list|)
block|,
name|NORMAL
argument_list|(
literal|1
argument_list|)
block|,
name|VERBOSE
argument_list|(
literal|2
argument_list|)
block|;
DECL|field|level
specifier|private
specifier|final
name|int
name|level
decl_stmt|;
DECL|method|Verbosity
specifier|private
name|Verbosity
parameter_list|(
name|int
name|level
parameter_list|)
block|{
name|this
operator|.
name|level
operator|=
name|level
expr_stmt|;
block|}
DECL|method|enabled
specifier|public
name|boolean
name|enabled
parameter_list|(
name|Verbosity
name|verbosity
parameter_list|)
block|{
return|return
name|level
operator|>=
name|verbosity
operator|.
name|level
return|;
block|}
DECL|method|resolve
specifier|public
specifier|static
name|Verbosity
name|resolve
parameter_list|(
name|CommandLine
name|cli
parameter_list|)
block|{
if|if
condition|(
name|cli
operator|.
name|hasOption
argument_list|(
literal|"s"
argument_list|)
condition|)
block|{
return|return
name|SILENT
return|;
block|}
if|if
condition|(
name|cli
operator|.
name|hasOption
argument_list|(
literal|"v"
argument_list|)
condition|)
block|{
return|return
name|VERBOSE
return|;
block|}
return|return
name|NORMAL
return|;
block|}
block|}
DECL|field|verbosity
specifier|private
name|Verbosity
name|verbosity
init|=
name|Verbosity
operator|.
name|NORMAL
decl_stmt|;
DECL|field|isDebugEnabled
specifier|private
specifier|final
name|boolean
name|isDebugEnabled
decl_stmt|;
DECL|method|Terminal
specifier|public
name|Terminal
parameter_list|()
block|{
name|this
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|)
expr_stmt|;
block|}
DECL|method|Terminal
specifier|public
name|Terminal
parameter_list|(
name|Verbosity
name|verbosity
parameter_list|)
block|{
name|this
operator|.
name|verbosity
operator|=
name|verbosity
expr_stmt|;
name|this
operator|.
name|isDebugEnabled
operator|=
literal|"true"
operator|.
name|equals
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
name|DEBUG_SYSTEM_PROPERTY
argument_list|,
literal|"false"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|verbosity
specifier|public
name|void
name|verbosity
parameter_list|(
name|Verbosity
name|verbosity
parameter_list|)
block|{
name|this
operator|.
name|verbosity
operator|=
name|verbosity
expr_stmt|;
block|}
DECL|method|verbosity
specifier|public
name|Verbosity
name|verbosity
parameter_list|()
block|{
return|return
name|verbosity
return|;
block|}
DECL|method|readText
specifier|public
specifier|abstract
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
function_decl|;
DECL|method|readSecret
specifier|public
specifier|abstract
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
function_decl|;
DECL|method|printStackTrace
specifier|protected
specifier|abstract
name|void
name|printStackTrace
parameter_list|(
name|Throwable
name|t
parameter_list|)
function_decl|;
DECL|method|println
specifier|public
name|void
name|println
parameter_list|()
block|{
name|println
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|)
expr_stmt|;
block|}
DECL|method|println
specifier|public
name|void
name|println
parameter_list|(
name|String
name|msg
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|println
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|,
name|msg
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
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
name|print
argument_list|(
name|Verbosity
operator|.
name|NORMAL
argument_list|,
name|msg
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|println
specifier|public
name|void
name|println
parameter_list|(
name|Verbosity
name|verbosity
parameter_list|)
block|{
name|println
argument_list|(
name|verbosity
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
DECL|method|println
specifier|public
name|void
name|println
parameter_list|(
name|Verbosity
name|verbosity
parameter_list|,
name|String
name|msg
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|print
argument_list|(
name|verbosity
argument_list|,
name|msg
operator|+
name|System
operator|.
name|lineSeparator
argument_list|()
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|print
specifier|public
name|void
name|print
parameter_list|(
name|Verbosity
name|verbosity
parameter_list|,
name|String
name|msg
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
if|if
condition|(
name|this
operator|.
name|verbosity
operator|.
name|enabled
argument_list|(
name|verbosity
argument_list|)
condition|)
block|{
name|doPrint
argument_list|(
name|msg
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|printError
specifier|public
name|void
name|printError
parameter_list|(
name|String
name|msg
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|println
argument_list|(
name|Verbosity
operator|.
name|SILENT
argument_list|,
literal|"ERROR: "
operator|+
name|msg
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|printError
specifier|public
name|void
name|printError
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|printError
argument_list|(
literal|"%s"
argument_list|,
name|t
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|isDebugEnabled
condition|)
block|{
name|printStackTrace
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|printWarn
specifier|public
name|void
name|printWarn
parameter_list|(
name|String
name|msg
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|println
argument_list|(
name|Verbosity
operator|.
name|SILENT
argument_list|,
literal|"WARN: "
operator|+
name|msg
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
DECL|method|doPrint
specifier|protected
specifier|abstract
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
function_decl|;
DECL|method|writer
specifier|public
specifier|abstract
name|PrintWriter
name|writer
parameter_list|()
function_decl|;
DECL|class|ConsoleTerminal
specifier|private
specifier|static
class|class
name|ConsoleTerminal
extends|extends
name|Terminal
block|{
DECL|field|console
specifier|final
name|Console
name|console
init|=
name|System
operator|.
name|console
argument_list|()
decl_stmt|;
DECL|method|supported
specifier|static
name|boolean
name|supported
parameter_list|()
block|{
return|return
name|System
operator|.
name|console
argument_list|()
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|doPrint
specifier|public
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
name|console
operator|.
name|printf
argument_list|(
name|msg
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|console
operator|.
name|flush
argument_list|()
expr_stmt|;
block|}
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
name|console
operator|.
name|readLine
argument_list|(
name|text
argument_list|,
name|args
argument_list|)
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
name|console
operator|.
name|readPassword
argument_list|(
name|text
argument_list|,
name|args
argument_list|)
return|;
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
name|console
operator|.
name|writer
argument_list|()
return|;
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
name|t
operator|.
name|printStackTrace
argument_list|(
name|console
operator|.
name|writer
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"System#out"
argument_list|)
DECL|class|SystemTerminal
specifier|private
specifier|static
class|class
name|SystemTerminal
extends|extends
name|Terminal
block|{
DECL|field|printWriter
specifier|private
specifier|final
name|PrintWriter
name|printWriter
init|=
operator|new
name|PrintWriter
argument_list|(
name|System
operator|.
name|out
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|doPrint
specifier|public
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
name|System
operator|.
name|out
operator|.
name|print
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
name|print
argument_list|(
name|text
argument_list|,
name|args
argument_list|)
expr_stmt|;
name|BufferedReader
name|reader
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|System
operator|.
name|in
argument_list|)
argument_list|)
decl_stmt|;
try|try
block|{
return|return
name|reader
operator|.
name|readLine
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
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
name|readText
argument_list|(
name|text
argument_list|,
name|args
argument_list|)
operator|.
name|toCharArray
argument_list|()
return|;
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
name|t
operator|.
name|printStackTrace
argument_list|(
name|printWriter
argument_list|)
expr_stmt|;
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
name|printWriter
return|;
block|}
block|}
block|}
end_class

end_unit

