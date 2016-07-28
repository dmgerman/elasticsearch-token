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
name|Definition
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
name|Globals
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
name|Locals
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
name|Location
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

begin_import
import|import
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Label
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
name|Opcodes
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * Represents an if block.  */
end_comment

begin_class
DECL|class|SIf
specifier|public
specifier|final
class|class
name|SIf
extends|extends
name|AStatement
block|{
DECL|field|condition
name|AExpression
name|condition
decl_stmt|;
DECL|field|ifblock
specifier|final
name|SBlock
name|ifblock
decl_stmt|;
DECL|method|SIf
specifier|public
name|SIf
parameter_list|(
name|Location
name|location
parameter_list|,
name|AExpression
name|condition
parameter_list|,
name|SBlock
name|ifblock
parameter_list|)
block|{
name|super
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|condition
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|condition
argument_list|)
expr_stmt|;
name|this
operator|.
name|ifblock
operator|=
name|ifblock
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|extractVariables
name|void
name|extractVariables
parameter_list|(
name|Set
argument_list|<
name|String
argument_list|>
name|variables
parameter_list|)
block|{
name|condition
operator|.
name|extractVariables
argument_list|(
name|variables
argument_list|)
expr_stmt|;
if|if
condition|(
name|ifblock
operator|!=
literal|null
condition|)
block|{
name|ifblock
operator|.
name|extractVariables
argument_list|(
name|variables
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|analyze
name|void
name|analyze
parameter_list|(
name|Locals
name|locals
parameter_list|)
block|{
name|condition
operator|.
name|expected
operator|=
name|Definition
operator|.
name|BOOLEAN_TYPE
expr_stmt|;
name|condition
operator|.
name|analyze
argument_list|(
name|locals
argument_list|)
expr_stmt|;
name|condition
operator|=
name|condition
operator|.
name|cast
argument_list|(
name|locals
argument_list|)
expr_stmt|;
if|if
condition|(
name|condition
operator|.
name|constant
operator|!=
literal|null
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Extraneous if statement."
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|ifblock
operator|==
literal|null
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Extraneous if statement."
argument_list|)
argument_list|)
throw|;
block|}
name|ifblock
operator|.
name|lastSource
operator|=
name|lastSource
expr_stmt|;
name|ifblock
operator|.
name|inLoop
operator|=
name|inLoop
expr_stmt|;
name|ifblock
operator|.
name|lastLoop
operator|=
name|lastLoop
expr_stmt|;
name|ifblock
operator|.
name|analyze
argument_list|(
name|Locals
operator|.
name|newLocalScope
argument_list|(
name|locals
argument_list|)
argument_list|)
expr_stmt|;
name|anyContinue
operator|=
name|ifblock
operator|.
name|anyContinue
expr_stmt|;
name|anyBreak
operator|=
name|ifblock
operator|.
name|anyBreak
expr_stmt|;
name|statementCount
operator|=
name|ifblock
operator|.
name|statementCount
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
parameter_list|,
name|Globals
name|globals
parameter_list|)
block|{
name|writer
operator|.
name|writeStatementOffset
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|Label
name|fals
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
name|condition
operator|.
name|write
argument_list|(
name|writer
argument_list|,
name|globals
argument_list|)
expr_stmt|;
name|writer
operator|.
name|ifZCmp
argument_list|(
name|Opcodes
operator|.
name|IFEQ
argument_list|,
name|fals
argument_list|)
expr_stmt|;
name|ifblock
operator|.
name|continu
operator|=
name|continu
expr_stmt|;
name|ifblock
operator|.
name|brake
operator|=
name|brake
expr_stmt|;
name|ifblock
operator|.
name|write
argument_list|(
name|writer
argument_list|,
name|globals
argument_list|)
expr_stmt|;
name|writer
operator|.
name|mark
argument_list|(
name|fals
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

