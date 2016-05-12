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
name|elasticsearch
operator|.
name|painless
operator|.
name|Variables
operator|.
name|Reserved
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|node
operator|.
name|SSource
import|;
end_import

begin_comment
comment|/**  * Runs the analysis phase of compilation using the Painless AST.  */
end_comment

begin_class
DECL|class|Analyzer
specifier|final
class|class
name|Analyzer
block|{
DECL|method|analyze
specifier|static
name|Variables
name|analyze
parameter_list|(
specifier|final
name|CompilerSettings
name|settings
parameter_list|,
specifier|final
name|Definition
name|definition
parameter_list|,
specifier|final
name|Reserved
name|shortcut
parameter_list|,
specifier|final
name|SSource
name|root
parameter_list|)
block|{
specifier|final
name|Variables
name|variables
init|=
operator|new
name|Variables
argument_list|(
name|settings
argument_list|,
name|definition
argument_list|,
name|shortcut
argument_list|)
decl_stmt|;
name|root
operator|.
name|analyze
argument_list|(
name|settings
argument_list|,
name|definition
argument_list|,
name|variables
argument_list|)
expr_stmt|;
return|return
name|variables
return|;
block|}
DECL|method|Analyzer
specifier|private
name|Analyzer
parameter_list|()
block|{}
block|}
end_class

end_unit

