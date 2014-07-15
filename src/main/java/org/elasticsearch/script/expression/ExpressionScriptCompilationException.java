begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.expression
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|expression
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|ParseException
import|;
end_import

begin_comment
comment|/**  * Exception representing a compilation error in an expression.  */
end_comment

begin_class
DECL|class|ExpressionScriptCompilationException
specifier|public
class|class
name|ExpressionScriptCompilationException
extends|extends
name|ElasticsearchException
block|{
DECL|method|ExpressionScriptCompilationException
specifier|public
name|ExpressionScriptCompilationException
parameter_list|(
name|String
name|msg
parameter_list|,
name|ParseException
name|e
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
DECL|method|ExpressionScriptCompilationException
specifier|public
name|ExpressionScriptCompilationException
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|super
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

