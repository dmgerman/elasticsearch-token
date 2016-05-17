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
name|CompilerSettings
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
name|Variables
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
name|elasticsearch
operator|.
name|painless
operator|.
name|MethodWriter
import|;
end_import

begin_comment
comment|/**  * Represents a do-while loop.  */
end_comment

begin_class
DECL|class|SDo
specifier|public
specifier|final
class|class
name|SDo
extends|extends
name|AStatement
block|{
DECL|field|block
specifier|final
name|AStatement
name|block
decl_stmt|;
DECL|field|condition
name|AExpression
name|condition
decl_stmt|;
DECL|method|SDo
specifier|public
name|SDo
parameter_list|(
specifier|final
name|int
name|line
parameter_list|,
specifier|final
name|String
name|location
parameter_list|,
specifier|final
name|AStatement
name|block
parameter_list|,
specifier|final
name|AExpression
name|condition
parameter_list|)
block|{
name|super
argument_list|(
name|line
argument_list|,
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|condition
operator|=
name|condition
expr_stmt|;
name|this
operator|.
name|block
operator|=
name|block
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|analyze
name|void
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
name|Variables
name|variables
parameter_list|)
block|{
name|variables
operator|.
name|incrementScope
argument_list|()
expr_stmt|;
name|block
operator|.
name|beginLoop
operator|=
literal|true
expr_stmt|;
name|block
operator|.
name|inLoop
operator|=
literal|true
expr_stmt|;
name|block
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
if|if
condition|(
name|block
operator|.
name|loopEscape
operator|&&
operator|!
name|block
operator|.
name|anyContinue
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Extraneous do while loop."
argument_list|)
argument_list|)
throw|;
block|}
name|condition
operator|.
name|expected
operator|=
name|definition
operator|.
name|booleanType
expr_stmt|;
name|condition
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
name|condition
operator|=
name|condition
operator|.
name|cast
argument_list|(
name|settings
argument_list|,
name|definition
argument_list|,
name|variables
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
specifier|final
name|boolean
name|continuous
init|=
operator|(
name|boolean
operator|)
name|condition
operator|.
name|constant
decl_stmt|;
if|if
condition|(
operator|!
name|continuous
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Extraneous do while loop."
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
operator|!
name|block
operator|.
name|anyBreak
condition|)
block|{
name|methodEscape
operator|=
literal|true
expr_stmt|;
name|allEscape
operator|=
literal|true
expr_stmt|;
block|}
block|}
name|statementCount
operator|=
literal|1
expr_stmt|;
if|if
condition|(
name|settings
operator|.
name|getMaxLoopCounter
argument_list|()
operator|>
literal|0
condition|)
block|{
name|loopCounterSlot
operator|=
name|variables
operator|.
name|getVariable
argument_list|(
name|location
argument_list|,
literal|"#loop"
argument_list|)
operator|.
name|slot
expr_stmt|;
block|}
name|variables
operator|.
name|decrementScope
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|write
name|void
name|write
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
name|MethodWriter
name|adapter
parameter_list|)
block|{
name|writeDebugInfo
argument_list|(
name|adapter
argument_list|)
expr_stmt|;
specifier|final
name|Label
name|start
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
specifier|final
name|Label
name|begin
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
specifier|final
name|Label
name|end
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
name|adapter
operator|.
name|mark
argument_list|(
name|start
argument_list|)
expr_stmt|;
name|block
operator|.
name|continu
operator|=
name|begin
expr_stmt|;
name|block
operator|.
name|brake
operator|=
name|end
expr_stmt|;
name|block
operator|.
name|write
argument_list|(
name|settings
argument_list|,
name|definition
argument_list|,
name|adapter
argument_list|)
expr_stmt|;
name|adapter
operator|.
name|mark
argument_list|(
name|begin
argument_list|)
expr_stmt|;
name|condition
operator|.
name|fals
operator|=
name|end
expr_stmt|;
name|condition
operator|.
name|write
argument_list|(
name|settings
argument_list|,
name|definition
argument_list|,
name|adapter
argument_list|)
expr_stmt|;
name|adapter
operator|.
name|writeLoopCounter
argument_list|(
name|loopCounterSlot
argument_list|,
name|Math
operator|.
name|max
argument_list|(
literal|1
argument_list|,
name|block
operator|.
name|statementCount
argument_list|)
argument_list|)
expr_stmt|;
name|adapter
operator|.
name|goTo
argument_list|(
name|start
argument_list|)
expr_stmt|;
name|adapter
operator|.
name|mark
argument_list|(
name|end
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

