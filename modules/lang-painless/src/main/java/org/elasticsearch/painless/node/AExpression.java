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
name|Definition
operator|.
name|Cast
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
comment|/**  * The superclass for all E* (expression) nodes.  */
end_comment

begin_class
DECL|class|AExpression
specifier|public
specifier|abstract
class|class
name|AExpression
extends|extends
name|ANode
block|{
comment|/**      * Set to false when an expression will not be read from such as      * a basic assignment.  Note this variable is always set by the parent      * as input.      */
DECL|field|read
specifier|protected
name|boolean
name|read
init|=
literal|true
decl_stmt|;
comment|/**      * Set to true when an expression can be considered a stand alone      * statement.  Used to prevent extraneous bytecode. This is always      * set by the node as output.      */
DECL|field|statement
specifier|protected
name|boolean
name|statement
init|=
literal|false
decl_stmt|;
comment|/**      * Set to the expected type this node needs to be.  Note this variable      * is always set by the parent as input and should never be read from.      */
DECL|field|expected
specifier|protected
name|Type
name|expected
init|=
literal|null
decl_stmt|;
comment|/**      * Set to the actual type this node is.  Note this variable is always      * set by the node as output and should only be read from outside of the      * node itself.<b>Also, actual can always be read after a cast is      * called on this node to get the type of the node after the cast.</b>      */
DECL|field|actual
specifier|protected
name|Type
name|actual
init|=
literal|null
decl_stmt|;
comment|/**      * Set by {@link EExplicit} if a cast made on an expression node should be      * explicit.      */
DECL|field|explicit
specifier|protected
name|boolean
name|explicit
init|=
literal|false
decl_stmt|;
comment|/**      * Set to the value of the constant this expression node represents if      * and only if the node represents a constant.  If this is not null      * this node will be replaced by an {@link EConstant} during casting      * if it's not already one.      */
DECL|field|constant
specifier|protected
name|Object
name|constant
init|=
literal|null
decl_stmt|;
comment|/**      * Set to true by {@link ENull} to represent a null value.      */
DECL|field|isNull
specifier|protected
name|boolean
name|isNull
init|=
literal|false
decl_stmt|;
comment|/**      * If an expression represents a branch statement, represents the jump should      * the expression evaluate to a true value.  It should always be the case that only      * one of tru and fals are non-null or both are null.  Only used during the writing phase.      */
DECL|field|tru
specifier|protected
name|Label
name|tru
init|=
literal|null
decl_stmt|;
comment|/**      * If an expression represents a branch statement, represents the jump should      * the expression evaluate to a false value.  It should always be the case that only      * one of tru and fals are non-null or both are null.  Only used during the writing phase.      */
DECL|field|fals
specifier|protected
name|Label
name|fals
init|=
literal|null
decl_stmt|;
DECL|method|AExpression
specifier|public
name|AExpression
parameter_list|(
specifier|final
name|int
name|line
parameter_list|,
specifier|final
name|String
name|location
parameter_list|)
block|{
name|super
argument_list|(
name|line
argument_list|,
name|location
argument_list|)
expr_stmt|;
block|}
comment|/**      * Checks for errors and collects data for the writing phase.      */
DECL|method|analyze
specifier|abstract
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
function_decl|;
comment|/**      * Writes ASM based on the data collected during the analysis phase.      */
DECL|method|write
specifier|abstract
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
function_decl|;
comment|/**      * Inserts {@link ECast} nodes into the tree for implicit casts.  Also replaces      * nodes with the constant variable set to a non-null value with {@link EConstant}.      * @return The new child node for the parent node calling this method.      */
DECL|method|cast
name|AExpression
name|cast
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
specifier|final
name|Cast
name|cast
init|=
name|AnalyzerCaster
operator|.
name|getLegalCast
argument_list|(
name|definition
argument_list|,
name|location
argument_list|,
name|actual
argument_list|,
name|expected
argument_list|,
name|explicit
argument_list|)
decl_stmt|;
if|if
condition|(
name|cast
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|constant
operator|==
literal|null
operator|||
name|this
operator|instanceof
name|EConstant
condition|)
block|{
return|return
name|this
return|;
block|}
else|else
block|{
specifier|final
name|EConstant
name|econstant
init|=
operator|new
name|EConstant
argument_list|(
name|line
argument_list|,
name|location
argument_list|,
name|constant
argument_list|)
decl_stmt|;
name|econstant
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
operator|!
name|expected
operator|.
name|equals
argument_list|(
name|econstant
operator|.
name|actual
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|error
argument_list|(
literal|"Illegal tree structure."
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|econstant
return|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|constant
operator|==
literal|null
condition|)
block|{
specifier|final
name|ECast
name|ecast
init|=
operator|new
name|ECast
argument_list|(
name|line
argument_list|,
name|location
argument_list|,
name|this
argument_list|,
name|cast
argument_list|)
decl_stmt|;
name|ecast
operator|.
name|statement
operator|=
name|statement
expr_stmt|;
name|ecast
operator|.
name|actual
operator|=
name|expected
expr_stmt|;
name|ecast
operator|.
name|isNull
operator|=
name|isNull
expr_stmt|;
return|return
name|ecast
return|;
block|}
else|else
block|{
if|if
condition|(
name|expected
operator|.
name|sort
operator|.
name|constant
condition|)
block|{
name|constant
operator|=
name|AnalyzerCaster
operator|.
name|constCast
argument_list|(
name|location
argument_list|,
name|constant
argument_list|,
name|cast
argument_list|)
expr_stmt|;
specifier|final
name|EConstant
name|econstant
init|=
operator|new
name|EConstant
argument_list|(
name|line
argument_list|,
name|location
argument_list|,
name|constant
argument_list|)
decl_stmt|;
name|econstant
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
operator|!
name|expected
operator|.
name|equals
argument_list|(
name|econstant
operator|.
name|actual
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|error
argument_list|(
literal|"Illegal tree structure."
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|econstant
return|;
block|}
elseif|else
if|if
condition|(
name|this
operator|instanceof
name|EConstant
condition|)
block|{
specifier|final
name|ECast
name|ecast
init|=
operator|new
name|ECast
argument_list|(
name|line
argument_list|,
name|location
argument_list|,
name|this
argument_list|,
name|cast
argument_list|)
decl_stmt|;
name|ecast
operator|.
name|actual
operator|=
name|expected
expr_stmt|;
return|return
name|ecast
return|;
block|}
else|else
block|{
specifier|final
name|EConstant
name|econstant
init|=
operator|new
name|EConstant
argument_list|(
name|line
argument_list|,
name|location
argument_list|,
name|constant
argument_list|)
decl_stmt|;
name|econstant
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
operator|!
name|actual
operator|.
name|equals
argument_list|(
name|econstant
operator|.
name|actual
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|error
argument_list|(
literal|"Illegal tree structure."
argument_list|)
argument_list|)
throw|;
block|}
specifier|final
name|ECast
name|ecast
init|=
operator|new
name|ECast
argument_list|(
name|line
argument_list|,
name|location
argument_list|,
name|econstant
argument_list|,
name|cast
argument_list|)
decl_stmt|;
name|ecast
operator|.
name|actual
operator|=
name|expected
expr_stmt|;
return|return
name|ecast
return|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

