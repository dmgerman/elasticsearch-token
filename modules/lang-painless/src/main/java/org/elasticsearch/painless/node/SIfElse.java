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
name|objectweb
operator|.
name|asm
operator|.
name|commons
operator|.
name|GeneratorAdapter
import|;
end_import

begin_comment
comment|/**  * Represents an if/else block.  */
end_comment

begin_class
DECL|class|SIfElse
specifier|public
specifier|final
class|class
name|SIfElse
extends|extends
name|AStatement
block|{
DECL|field|condition
name|AExpression
name|condition
decl_stmt|;
DECL|field|ifblock
specifier|final
name|AStatement
name|ifblock
decl_stmt|;
DECL|field|elseblock
specifier|final
name|AStatement
name|elseblock
decl_stmt|;
DECL|method|SIfElse
specifier|public
name|SIfElse
parameter_list|(
specifier|final
name|String
name|location
parameter_list|,
specifier|final
name|AExpression
name|condition
parameter_list|,
specifier|final
name|AStatement
name|ifblock
parameter_list|,
specifier|final
name|AStatement
name|elseblock
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
name|condition
expr_stmt|;
name|this
operator|.
name|ifblock
operator|=
name|ifblock
expr_stmt|;
name|this
operator|.
name|elseblock
operator|=
name|elseblock
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
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
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
name|variables
operator|.
name|incrementScope
argument_list|()
expr_stmt|;
name|ifblock
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
name|variables
operator|.
name|decrementScope
argument_list|()
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
if|if
condition|(
name|elseblock
operator|!=
literal|null
condition|)
block|{
name|elseblock
operator|.
name|lastSource
operator|=
name|lastSource
expr_stmt|;
name|elseblock
operator|.
name|inLoop
operator|=
name|inLoop
expr_stmt|;
name|elseblock
operator|.
name|lastLoop
operator|=
name|lastLoop
expr_stmt|;
name|variables
operator|.
name|incrementScope
argument_list|()
expr_stmt|;
name|elseblock
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
name|variables
operator|.
name|decrementScope
argument_list|()
expr_stmt|;
name|methodEscape
operator|=
name|ifblock
operator|.
name|methodEscape
operator|&&
name|elseblock
operator|.
name|methodEscape
expr_stmt|;
name|loopEscape
operator|=
name|ifblock
operator|.
name|loopEscape
operator|&&
name|elseblock
operator|.
name|loopEscape
expr_stmt|;
name|allEscape
operator|=
name|ifblock
operator|.
name|allEscape
operator|&&
name|elseblock
operator|.
name|allEscape
expr_stmt|;
name|anyContinue
operator||=
name|elseblock
operator|.
name|anyContinue
expr_stmt|;
name|anyBreak
operator||=
name|elseblock
operator|.
name|anyBreak
expr_stmt|;
name|statementCount
operator|=
name|Math
operator|.
name|max
argument_list|(
name|ifblock
operator|.
name|statementCount
argument_list|,
name|elseblock
operator|.
name|statementCount
argument_list|)
expr_stmt|;
block|}
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
name|GeneratorAdapter
name|adapter
parameter_list|)
block|{
specifier|final
name|Label
name|end
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
specifier|final
name|Label
name|fals
init|=
name|elseblock
operator|!=
literal|null
condition|?
operator|new
name|Label
argument_list|()
else|:
name|end
decl_stmt|;
name|condition
operator|.
name|fals
operator|=
name|fals
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
name|settings
argument_list|,
name|definition
argument_list|,
name|adapter
argument_list|)
expr_stmt|;
if|if
condition|(
name|elseblock
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
operator|!
name|ifblock
operator|.
name|allEscape
condition|)
block|{
name|adapter
operator|.
name|goTo
argument_list|(
name|end
argument_list|)
expr_stmt|;
block|}
name|adapter
operator|.
name|mark
argument_list|(
name|fals
argument_list|)
expr_stmt|;
name|elseblock
operator|.
name|continu
operator|=
name|continu
expr_stmt|;
name|elseblock
operator|.
name|brake
operator|=
name|brake
expr_stmt|;
name|elseblock
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
block|}
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

