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
name|AnalyzerCaster
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
operator|.
name|Type
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Objects
operator|.
name|requireNonNull
import|;
end_import

begin_comment
comment|/**  * The Elvis operator ({@code ?:}), a null coalescing operator. Binary operator that evaluates the first expression and return it if it is  * non null. If the first expression is null then it evaluates the second expression and returns it.  */
end_comment

begin_class
DECL|class|EElvis
specifier|public
class|class
name|EElvis
extends|extends
name|AExpression
block|{
DECL|field|lhs
specifier|private
name|AExpression
name|lhs
decl_stmt|;
DECL|field|rhs
specifier|private
name|AExpression
name|rhs
decl_stmt|;
DECL|method|EElvis
specifier|public
name|EElvis
parameter_list|(
name|Location
name|location
parameter_list|,
name|AExpression
name|lhs
parameter_list|,
name|AExpression
name|rhs
parameter_list|)
block|{
name|super
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|lhs
operator|=
name|requireNonNull
argument_list|(
name|lhs
argument_list|)
expr_stmt|;
name|this
operator|.
name|rhs
operator|=
name|requireNonNull
argument_list|(
name|rhs
argument_list|)
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
name|lhs
operator|.
name|extractVariables
argument_list|(
name|variables
argument_list|)
expr_stmt|;
name|rhs
operator|.
name|extractVariables
argument_list|(
name|variables
argument_list|)
expr_stmt|;
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
if|if
condition|(
name|expected
operator|!=
literal|null
operator|&&
name|expected
operator|.
name|sort
operator|.
name|primitive
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Evlis operator cannot return primitives"
argument_list|)
argument_list|)
throw|;
block|}
name|lhs
operator|.
name|expected
operator|=
name|expected
expr_stmt|;
name|lhs
operator|.
name|explicit
operator|=
name|explicit
expr_stmt|;
name|lhs
operator|.
name|internal
operator|=
name|internal
expr_stmt|;
name|rhs
operator|.
name|expected
operator|=
name|expected
expr_stmt|;
name|rhs
operator|.
name|explicit
operator|=
name|explicit
expr_stmt|;
name|rhs
operator|.
name|internal
operator|=
name|internal
expr_stmt|;
name|actual
operator|=
name|expected
expr_stmt|;
name|lhs
operator|.
name|analyze
argument_list|(
name|locals
argument_list|)
expr_stmt|;
name|rhs
operator|.
name|analyze
argument_list|(
name|locals
argument_list|)
expr_stmt|;
if|if
condition|(
name|lhs
operator|.
name|isNull
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Extraneous elvis operator. LHS is null."
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|lhs
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
literal|"Extraneous elvis operator. LHS is a constant."
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|lhs
operator|.
name|actual
operator|.
name|sort
operator|.
name|primitive
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Extraneous elvis operator. LHS is a primitive."
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|rhs
operator|.
name|isNull
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Extraneous elvis operator. RHS is null."
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|expected
operator|==
literal|null
condition|)
block|{
specifier|final
name|Type
name|promote
init|=
name|AnalyzerCaster
operator|.
name|promoteConditional
argument_list|(
name|lhs
operator|.
name|actual
argument_list|,
name|rhs
operator|.
name|actual
argument_list|,
name|lhs
operator|.
name|constant
argument_list|,
name|rhs
operator|.
name|constant
argument_list|)
decl_stmt|;
name|lhs
operator|.
name|expected
operator|=
name|promote
expr_stmt|;
name|rhs
operator|.
name|expected
operator|=
name|promote
expr_stmt|;
name|actual
operator|=
name|promote
expr_stmt|;
block|}
name|lhs
operator|=
name|lhs
operator|.
name|cast
argument_list|(
name|locals
argument_list|)
expr_stmt|;
name|rhs
operator|=
name|rhs
operator|.
name|cast
argument_list|(
name|locals
argument_list|)
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
name|writeDebugInfo
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|Label
name|end
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
name|lhs
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
name|dup
argument_list|()
expr_stmt|;
name|writer
operator|.
name|ifNonNull
argument_list|(
name|end
argument_list|)
expr_stmt|;
name|writer
operator|.
name|pop
argument_list|()
expr_stmt|;
name|rhs
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
name|end
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|singleLineToString
argument_list|(
name|lhs
argument_list|,
name|rhs
argument_list|)
return|;
block|}
block|}
end_class

end_unit

