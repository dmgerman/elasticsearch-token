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
name|Definition
operator|.
name|Method
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
name|Struct
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
name|List
import|;
end_import

begin_comment
comment|/**  * Represents and object instantiation.  */
end_comment

begin_class
DECL|class|LNewObj
specifier|public
specifier|final
class|class
name|LNewObj
extends|extends
name|ALink
block|{
DECL|field|type
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|arguments
specifier|final
name|List
argument_list|<
name|AExpression
argument_list|>
name|arguments
decl_stmt|;
DECL|field|constructor
name|Method
name|constructor
decl_stmt|;
DECL|method|LNewObj
specifier|public
name|LNewObj
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
parameter_list|)
block|{
name|super
argument_list|(
name|location
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|arguments
operator|=
name|arguments
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|analyze
name|ALink
name|analyze
parameter_list|(
name|Locals
name|locals
parameter_list|)
block|{
if|if
condition|(
name|before
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
literal|"Illegal new call with a target already defined."
argument_list|)
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|store
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot assign a value to a new call."
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
name|Definition
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
name|Struct
name|struct
init|=
name|type
operator|.
name|struct
decl_stmt|;
name|constructor
operator|=
name|struct
operator|.
name|constructors
operator|.
name|get
argument_list|(
operator|new
name|Definition
operator|.
name|MethodKey
argument_list|(
literal|"<init>"
argument_list|,
name|arguments
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|constructor
operator|!=
literal|null
condition|)
block|{
name|Type
index|[]
name|types
init|=
operator|new
name|Type
index|[
name|constructor
operator|.
name|arguments
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|constructor
operator|.
name|arguments
operator|.
name|toArray
argument_list|(
name|types
argument_list|)
expr_stmt|;
if|if
condition|(
name|constructor
operator|.
name|arguments
operator|.
name|size
argument_list|()
operator|!=
name|arguments
operator|.
name|size
argument_list|()
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"When calling constructor on type ["
operator|+
name|struct
operator|.
name|name
operator|+
literal|"]"
operator|+
literal|" expected ["
operator|+
name|constructor
operator|.
name|arguments
operator|.
name|size
argument_list|()
operator|+
literal|"] arguments, but found ["
operator|+
name|arguments
operator|.
name|size
argument_list|()
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
name|types
index|[
name|argument
index|]
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
name|statement
operator|=
literal|true
expr_stmt|;
name|after
operator|=
name|type
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown new call on type ["
operator|+
name|struct
operator|.
name|name
operator|+
literal|"]."
argument_list|)
argument_list|)
throw|;
block|}
return|return
name|this
return|;
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
comment|// Do nothing.
block|}
annotation|@
name|Override
DECL|method|load
name|void
name|load
parameter_list|(
name|MethodWriter
name|writer
parameter_list|)
block|{
name|writer
operator|.
name|writeDebugInfo
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|writer
operator|.
name|newInstance
argument_list|(
name|after
operator|.
name|type
argument_list|)
expr_stmt|;
if|if
condition|(
name|load
condition|)
block|{
name|writer
operator|.
name|dup
argument_list|()
expr_stmt|;
block|}
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
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|invokeConstructor
argument_list|(
name|constructor
operator|.
name|owner
operator|.
name|type
argument_list|,
name|constructor
operator|.
name|method
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|store
name|void
name|store
parameter_list|(
name|MethodWriter
name|writer
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

