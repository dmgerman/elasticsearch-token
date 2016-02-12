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
name|common
operator|.
name|io
operator|.
name|Streams
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
name|util
operator|.
name|Callback
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
name|InputStream
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|HelpPrinter
specifier|public
class|class
name|HelpPrinter
block|{
DECL|field|HELP_FILE_EXT
specifier|private
specifier|static
specifier|final
name|String
name|HELP_FILE_EXT
init|=
literal|".help"
decl_stmt|;
DECL|method|print
specifier|public
name|void
name|print
parameter_list|(
name|CliToolConfig
name|config
parameter_list|,
name|Terminal
name|terminal
parameter_list|)
block|{
name|print
argument_list|(
name|config
operator|.
name|toolType
argument_list|()
argument_list|,
name|config
operator|.
name|name
argument_list|()
argument_list|,
name|terminal
argument_list|)
expr_stmt|;
block|}
DECL|method|print
specifier|public
name|void
name|print
parameter_list|(
name|String
name|toolName
parameter_list|,
name|CliToolConfig
operator|.
name|Cmd
name|cmd
parameter_list|,
name|Terminal
name|terminal
parameter_list|)
block|{
name|print
argument_list|(
name|cmd
operator|.
name|cmdType
argument_list|()
argument_list|,
name|toolName
operator|+
literal|"-"
operator|+
name|cmd
operator|.
name|name
argument_list|()
argument_list|,
name|terminal
argument_list|)
expr_stmt|;
block|}
DECL|method|print
specifier|private
specifier|static
name|void
name|print
parameter_list|(
name|Class
name|clazz
parameter_list|,
name|String
name|name
parameter_list|,
specifier|final
name|Terminal
name|terminal
parameter_list|)
block|{
name|terminal
operator|.
name|println
argument_list|(
name|Terminal
operator|.
name|Verbosity
operator|.
name|SILENT
argument_list|,
literal|""
argument_list|)
expr_stmt|;
try|try
init|(
name|InputStream
name|input
init|=
name|clazz
operator|.
name|getResourceAsStream
argument_list|(
name|name
operator|+
name|HELP_FILE_EXT
argument_list|)
init|)
block|{
name|Streams
operator|.
name|readAllLines
argument_list|(
name|input
argument_list|,
operator|new
name|Callback
argument_list|<
name|String
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|handle
parameter_list|(
name|String
name|line
parameter_list|)
block|{
name|terminal
operator|.
name|println
argument_list|(
name|Terminal
operator|.
name|Verbosity
operator|.
name|SILENT
argument_list|,
name|line
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
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
name|terminal
operator|.
name|println
argument_list|(
name|Terminal
operator|.
name|Verbosity
operator|.
name|SILENT
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

