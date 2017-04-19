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
name|java
operator|.
name|util
operator|.
name|List
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
comment|/**  * Represents an array instantiation.  */
end_comment

begin_class
DECL|class|ENewArray
specifier|public
specifier|final
class|class
name|ENewArray
extends|extends
name|AExpression
block|{
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|arguments
specifier|private
specifier|final
name|List
argument_list|<
name|AExpression
argument_list|>
name|arguments
decl_stmt|;
DECL|field|initialize
specifier|private
specifier|final
name|boolean
name|initialize
decl_stmt|;
DECL|method|ENewArray
specifier|public
name|ENewArray
parameter_list|(
name|Location
name|location
parameter_list|,
name|String
name|type
parameter_list|,
name|List
argument_list|<
name|AExpression
argument_list|>
name|arguments
parameter_list|,
name|boolean
name|initialize
parameter_list|)
block|{
name|super
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|this
operator|.
name|arguments
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|arguments
argument_list|)
expr_stmt|;
name|this
operator|.
name|initialize
operator|=
name|initialize
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
for|for
control|(
name|AExpression
name|argument
range|:
name|arguments
control|)
block|{
name|argument
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
literal|"A newly created array must be read from."
argument_list|)
argument_list|)
throw|;
block|}
specifier|final
name|Type
name|type
decl_stmt|;
try|try
block|{
name|type
operator|=
name|locals
operator|.
name|getDefinition
argument_list|()
operator|.
name|getType
argument_list|(
name|this
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|exception
parameter_list|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Not a type ["
operator|+
name|this
operator|.
name|type
operator|+
literal|"]."
argument_list|)
argument_list|)
throw|;
block|}
for|for
control|(
name|int
name|argument
init|=
literal|0
init|;
name|argument
operator|<
name|arguments
operator|.
name|size
argument_list|()
condition|;
operator|++
name|argument
control|)
block|{
name|AExpression
name|expression
init|=
name|arguments
operator|.
name|get
argument_list|(
name|argument
argument_list|)
decl_stmt|;
name|expression
operator|.
name|expected
operator|=
name|initialize
condition|?
name|locals
operator|.
name|getDefinition
argument_list|()
operator|.
name|getType
argument_list|(
name|type
operator|.
name|struct
argument_list|,
literal|0
argument_list|)
else|:
name|Definition
operator|.
name|INT_TYPE
expr_stmt|;
name|expression
operator|.
name|internal
operator|=
literal|true
expr_stmt|;
name|expression
operator|.
name|analyze
argument_list|(
name|locals
argument_list|)
expr_stmt|;
name|arguments
operator|.
name|set
argument_list|(
name|argument
argument_list|,
name|expression
operator|.
name|cast
argument_list|(
name|locals
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|actual
operator|=
name|locals
operator|.
name|getDefinition
argument_list|()
operator|.
name|getType
argument_list|(
name|type
operator|.
name|struct
argument_list|,
name|initialize
condition|?
literal|1
else|:
name|arguments
operator|.
name|size
argument_list|()
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
if|if
condition|(
name|initialize
condition|)
block|{
name|writer
operator|.
name|push
argument_list|(
name|arguments
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|writer
operator|.
name|newArray
argument_list|(
name|actual
operator|.
name|struct
operator|.
name|type
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|index
init|=
literal|0
init|;
name|index
operator|<
name|arguments
operator|.
name|size
argument_list|()
condition|;
operator|++
name|index
control|)
block|{
name|AExpression
name|argument
init|=
name|arguments
operator|.
name|get
argument_list|(
name|index
argument_list|)
decl_stmt|;
name|writer
operator|.
name|dup
argument_list|()
expr_stmt|;
name|writer
operator|.
name|push
argument_list|(
name|index
argument_list|)
expr_stmt|;
name|argument
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
name|arrayStore
argument_list|(
name|actual
operator|.
name|struct
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
for|for
control|(
name|AExpression
name|argument
range|:
name|arguments
control|)
block|{
name|argument
operator|.
name|write
argument_list|(
name|writer
argument_list|,
name|globals
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|arguments
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
name|writer
operator|.
name|visitMultiANewArrayInsn
argument_list|(
name|actual
operator|.
name|type
operator|.
name|getDescriptor
argument_list|()
argument_list|,
name|actual
operator|.
name|type
operator|.
name|getDimensions
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|writer
operator|.
name|newArray
argument_list|(
name|actual
operator|.
name|struct
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
block|}
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
name|singleLineToStringWithOptionalArgs
argument_list|(
name|arguments
argument_list|,
name|type
argument_list|,
name|initialize
condition|?
literal|"init"
else|:
literal|"dims"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

