begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.painless
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|painless
package|;
end_package

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
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|util
operator|.
name|Textifier
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
name|StringWriter
import|;
end_import

begin_comment
comment|/** quick and dirty tools for debugging */
end_comment

begin_class
DECL|class|Debugger
specifier|final
class|class
name|Debugger
block|{
comment|/** compiles source to bytecode, and returns debugging output */
DECL|method|toString
specifier|static
name|String
name|toString
parameter_list|(
specifier|final
name|String
name|source
parameter_list|)
block|{
return|return
name|toString
argument_list|(
name|GenericElasticsearchScript
operator|.
name|class
argument_list|,
name|source
argument_list|,
operator|new
name|CompilerSettings
argument_list|()
argument_list|)
return|;
block|}
comment|/** compiles to bytecode, and returns debugging output */
DECL|method|toString
specifier|static
name|String
name|toString
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|iface
parameter_list|,
name|String
name|source
parameter_list|,
name|CompilerSettings
name|settings
parameter_list|)
block|{
name|StringWriter
name|output
init|=
operator|new
name|StringWriter
argument_list|()
decl_stmt|;
name|PrintWriter
name|outputWriter
init|=
operator|new
name|PrintWriter
argument_list|(
name|output
argument_list|)
decl_stmt|;
name|Textifier
name|textifier
init|=
operator|new
name|Textifier
argument_list|()
decl_stmt|;
try|try
block|{
name|Compiler
operator|.
name|compile
argument_list|(
name|iface
argument_list|,
literal|"<debugging>"
argument_list|,
name|source
argument_list|,
name|settings
argument_list|,
name|textifier
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|textifier
operator|.
name|print
argument_list|(
name|outputWriter
argument_list|)
expr_stmt|;
name|e
operator|.
name|addSuppressed
argument_list|(
operator|new
name|Exception
argument_list|(
literal|"current bytecode: \n"
operator|+
name|output
argument_list|)
argument_list|)
expr_stmt|;
name|IOUtils
operator|.
name|reThrowUnchecked
argument_list|(
name|e
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
name|textifier
operator|.
name|print
argument_list|(
name|outputWriter
argument_list|)
expr_stmt|;
return|return
name|output
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

