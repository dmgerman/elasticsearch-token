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
comment|/**  * Represents a set of statements as a branch of control-flow.  */
end_comment

begin_class
DECL|class|SBlock
specifier|public
specifier|final
class|class
name|SBlock
extends|extends
name|AStatement
block|{
DECL|field|statements
specifier|final
name|List
argument_list|<
name|AStatement
argument_list|>
name|statements
decl_stmt|;
DECL|method|SBlock
specifier|public
name|SBlock
parameter_list|(
name|Location
name|location
parameter_list|,
name|List
argument_list|<
name|AStatement
argument_list|>
name|statements
parameter_list|)
block|{
name|super
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|statements
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|statements
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
name|statements
operator|==
literal|null
operator|||
name|statements
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"A block must contain at least one statement."
argument_list|)
argument_list|)
throw|;
block|}
name|AStatement
name|last
init|=
name|statements
operator|.
name|get
argument_list|(
name|statements
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
for|for
control|(
name|AStatement
name|statement
range|:
name|statements
control|)
block|{
comment|// Note that we do not need to check after the last statement because
comment|// there is no statement that can be unreachable after the last.
if|if
condition|(
name|allEscape
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unreachable statement."
argument_list|)
argument_list|)
throw|;
block|}
name|statement
operator|.
name|inLoop
operator|=
name|inLoop
expr_stmt|;
name|statement
operator|.
name|lastSource
operator|=
name|lastSource
operator|&&
name|statement
operator|==
name|last
expr_stmt|;
name|statement
operator|.
name|lastLoop
operator|=
operator|(
name|beginLoop
operator|||
name|lastLoop
operator|)
operator|&&
name|statement
operator|==
name|last
expr_stmt|;
name|statement
operator|.
name|analyze
argument_list|(
name|variables
argument_list|)
expr_stmt|;
name|methodEscape
operator|=
name|statement
operator|.
name|methodEscape
expr_stmt|;
name|loopEscape
operator|=
name|statement
operator|.
name|loopEscape
expr_stmt|;
name|allEscape
operator|=
name|statement
operator|.
name|allEscape
expr_stmt|;
name|anyContinue
operator||=
name|statement
operator|.
name|anyContinue
expr_stmt|;
name|anyBreak
operator||=
name|statement
operator|.
name|anyBreak
expr_stmt|;
name|statementCount
operator|+=
name|statement
operator|.
name|statementCount
expr_stmt|;
block|}
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
for|for
control|(
name|AStatement
name|statement
range|:
name|statements
control|)
block|{
name|statement
operator|.
name|continu
operator|=
name|continu
expr_stmt|;
name|statement
operator|.
name|brake
operator|=
name|brake
expr_stmt|;
name|statement
operator|.
name|write
argument_list|(
name|writer
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

