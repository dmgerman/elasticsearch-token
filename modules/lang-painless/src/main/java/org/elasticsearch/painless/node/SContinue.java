begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.painless.node
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|node
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
name|MethodWriter
import|;
end_import

begin_comment
comment|/**  * Represents a continue statement.  */
end_comment

begin_class
DECL|class|SContinue
specifier|public
specifier|final
class|class
name|SContinue
extends|extends
name|AStatement
block|{
DECL|method|SContinue
specifier|public
name|SContinue
parameter_list|(
name|int
name|line
parameter_list|,
name|int
name|offset
parameter_list|,
name|String
name|location
parameter_list|)
block|{
name|super
argument_list|(
name|line
argument_list|,
name|offset
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|analyze
name|void
name|analyze
parameter_list|(
name|Variables
name|variables
parameter_list|)
block|{
if|if
condition|(
operator|!
name|inLoop
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Continue statement outside of a loop."
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|lastLoop
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Extraneous continue statement."
argument_list|)
argument_list|)
throw|;
block|}
name|allEscape
operator|=
literal|true
expr_stmt|;
name|anyContinue
operator|=
literal|true
expr_stmt|;
name|statementCount
operator|=
literal|1
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|write
name|void
name|write
parameter_list|(
name|MethodWriter
name|writer
parameter_list|)
block|{
name|writer
operator|.
name|goTo
argument_list|(
name|continu
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

