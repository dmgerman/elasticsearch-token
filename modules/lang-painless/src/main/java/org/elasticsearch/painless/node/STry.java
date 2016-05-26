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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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

begin_comment
comment|/**  * Represents the try block as part of a try-catch block.  */
end_comment

begin_class
DECL|class|STry
specifier|public
specifier|final
class|class
name|STry
extends|extends
name|AStatement
block|{
DECL|field|block
specifier|final
name|SBlock
name|block
decl_stmt|;
DECL|field|catches
specifier|final
name|List
argument_list|<
name|SCatch
argument_list|>
name|catches
decl_stmt|;
DECL|method|STry
specifier|public
name|STry
parameter_list|(
name|int
name|line
parameter_list|,
name|int
name|offset
parameter_list|,
name|String
name|location
parameter_list|,
name|SBlock
name|block
parameter_list|,
name|List
argument_list|<
name|SCatch
argument_list|>
name|traps
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
name|this
operator|.
name|block
operator|=
name|block
expr_stmt|;
name|this
operator|.
name|catches
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|traps
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
name|block
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Extraneous try statement."
argument_list|)
argument_list|)
throw|;
block|}
name|block
operator|.
name|lastSource
operator|=
name|lastSource
expr_stmt|;
name|block
operator|.
name|inLoop
operator|=
name|inLoop
expr_stmt|;
name|block
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
name|block
operator|.
name|analyze
argument_list|(
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
name|block
operator|.
name|methodEscape
expr_stmt|;
name|loopEscape
operator|=
name|block
operator|.
name|loopEscape
expr_stmt|;
name|allEscape
operator|=
name|block
operator|.
name|allEscape
expr_stmt|;
name|anyContinue
operator|=
name|block
operator|.
name|anyContinue
expr_stmt|;
name|anyBreak
operator|=
name|block
operator|.
name|anyBreak
expr_stmt|;
name|int
name|statementCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|SCatch
name|catc
range|:
name|catches
control|)
block|{
name|catc
operator|.
name|lastSource
operator|=
name|lastSource
expr_stmt|;
name|catc
operator|.
name|inLoop
operator|=
name|inLoop
expr_stmt|;
name|catc
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
name|catc
operator|.
name|analyze
argument_list|(
name|variables
argument_list|)
expr_stmt|;
name|variables
operator|.
name|decrementScope
argument_list|()
expr_stmt|;
name|methodEscape
operator|&=
name|catc
operator|.
name|methodEscape
expr_stmt|;
name|loopEscape
operator|&=
name|catc
operator|.
name|loopEscape
expr_stmt|;
name|allEscape
operator|&=
name|catc
operator|.
name|allEscape
expr_stmt|;
name|anyContinue
operator||=
name|catc
operator|.
name|anyContinue
expr_stmt|;
name|anyBreak
operator||=
name|catc
operator|.
name|anyBreak
expr_stmt|;
name|statementCount
operator|=
name|Math
operator|.
name|max
argument_list|(
name|statementCount
argument_list|,
name|catc
operator|.
name|statementCount
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|statementCount
operator|=
name|block
operator|.
name|statementCount
operator|+
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
parameter_list|)
block|{
name|writer
operator|.
name|writeStatementOffset
argument_list|(
name|offset
argument_list|)
expr_stmt|;
name|Label
name|begin
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
name|Label
name|end
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
name|Label
name|exception
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
name|writer
operator|.
name|mark
argument_list|(
name|begin
argument_list|)
expr_stmt|;
name|block
operator|.
name|continu
operator|=
name|continu
expr_stmt|;
name|block
operator|.
name|brake
operator|=
name|brake
expr_stmt|;
name|block
operator|.
name|write
argument_list|(
name|writer
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|block
operator|.
name|allEscape
condition|)
block|{
name|writer
operator|.
name|goTo
argument_list|(
name|exception
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|mark
argument_list|(
name|end
argument_list|)
expr_stmt|;
for|for
control|(
name|SCatch
name|catc
range|:
name|catches
control|)
block|{
name|catc
operator|.
name|begin
operator|=
name|begin
expr_stmt|;
name|catc
operator|.
name|end
operator|=
name|end
expr_stmt|;
name|catc
operator|.
name|exception
operator|=
name|catches
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|?
name|exception
else|:
literal|null
expr_stmt|;
name|catc
operator|.
name|write
argument_list|(
name|writer
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|block
operator|.
name|allEscape
operator|||
name|catches
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
name|writer
operator|.
name|mark
argument_list|(
name|exception
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

