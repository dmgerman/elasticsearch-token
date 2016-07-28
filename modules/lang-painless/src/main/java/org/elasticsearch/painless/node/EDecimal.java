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
name|MethodWriter
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
comment|/**  * Represents a decimal constant.  */
end_comment

begin_class
DECL|class|EDecimal
specifier|public
specifier|final
class|class
name|EDecimal
extends|extends
name|AExpression
block|{
DECL|field|value
specifier|private
specifier|final
name|String
name|value
decl_stmt|;
DECL|method|EDecimal
specifier|public
name|EDecimal
parameter_list|(
name|Location
name|location
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|value
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
block|{}
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
operator|!
name|read
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Must read from constant ["
operator|+
name|value
operator|+
literal|"]."
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
name|value
operator|.
name|endsWith
argument_list|(
literal|"f"
argument_list|)
operator|||
name|value
operator|.
name|endsWith
argument_list|(
literal|"F"
argument_list|)
condition|)
block|{
try|try
block|{
name|constant
operator|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|value
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|actual
operator|=
name|Definition
operator|.
name|FLOAT_TYPE
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|exception
parameter_list|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid float constant ["
operator|+
name|value
operator|+
literal|"]."
argument_list|)
argument_list|)
throw|;
block|}
block|}
else|else
block|{
try|try
block|{
name|constant
operator|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|actual
operator|=
name|Definition
operator|.
name|DOUBLE_TYPE
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NumberFormatException
name|exception
parameter_list|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid double constant ["
operator|+
name|value
operator|+
literal|"]."
argument_list|)
argument_list|)
throw|;
block|}
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
parameter_list|,
name|Globals
name|globals
parameter_list|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalStateException
argument_list|(
literal|"Illegal tree structure."
argument_list|)
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

