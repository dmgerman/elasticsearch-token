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
comment|/**  * Represents a method call or deferes to a def call.  */
end_comment

begin_class
DECL|class|LCall
specifier|public
specifier|final
class|class
name|LCall
extends|extends
name|ALink
block|{
DECL|field|name
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|arguments
specifier|final
name|List
argument_list|<
name|AExpression
argument_list|>
name|arguments
decl_stmt|;
DECL|field|method
name|Method
name|method
init|=
literal|null
decl_stmt|;
DECL|method|LCall
specifier|public
name|LCall
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
name|String
name|name
parameter_list|,
specifier|final
name|List
argument_list|<
name|AExpression
argument_list|>
name|arguments
parameter_list|)
block|{
name|super
argument_list|(
name|line
argument_list|,
name|location
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
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
specifier|final
name|CompilerSettings
name|settings
parameter_list|,
specifier|final
name|Variables
name|variables
parameter_list|)
block|{
if|if
condition|(
name|before
operator|==
literal|null
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
elseif|else
if|if
condition|(
name|before
operator|.
name|sort
operator|==
name|Definition
operator|.
name|Sort
operator|.
name|ARRAY
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Illegal call ["
operator|+
name|name
operator|+
literal|"] on array type."
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
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Cannot assign a value to a call ["
operator|+
name|name
operator|+
literal|"]."
argument_list|)
argument_list|)
throw|;
block|}
name|Definition
operator|.
name|MethodKey
name|methodKey
init|=
operator|new
name|Definition
operator|.
name|MethodKey
argument_list|(
name|name
argument_list|,
name|arguments
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Struct
name|struct
init|=
name|before
operator|.
name|struct
decl_stmt|;
name|method
operator|=
name|statik
condition|?
name|struct
operator|.
name|staticMethods
operator|.
name|get
argument_list|(
name|methodKey
argument_list|)
else|:
name|struct
operator|.
name|methods
operator|.
name|get
argument_list|(
name|methodKey
argument_list|)
expr_stmt|;
if|if
condition|(
name|method
operator|!=
literal|null
condition|)
block|{
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
specifier|final
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
name|method
operator|.
name|arguments
operator|.
name|get
argument_list|(
name|argument
argument_list|)
expr_stmt|;
name|expression
operator|.
name|analyze
argument_list|(
name|settings
argument_list|,
name|variables
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
name|settings
argument_list|,
name|variables
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
name|method
operator|.
name|rtn
expr_stmt|;
return|return
name|this
return|;
block|}
elseif|else
if|if
condition|(
name|before
operator|.
name|sort
operator|==
name|Definition
operator|.
name|Sort
operator|.
name|DEF
condition|)
block|{
specifier|final
name|ALink
name|link
init|=
operator|new
name|LDefCall
argument_list|(
name|line
argument_list|,
name|location
argument_list|,
name|name
argument_list|,
name|arguments
argument_list|)
decl_stmt|;
name|link
operator|.
name|copy
argument_list|(
name|this
argument_list|)
expr_stmt|;
return|return
name|link
operator|.
name|analyze
argument_list|(
name|settings
argument_list|,
name|variables
argument_list|)
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Unknown call ["
operator|+
name|name
operator|+
literal|"] with ["
operator|+
name|arguments
operator|.
name|size
argument_list|()
operator|+
literal|"] arguments on type ["
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
name|MethodWriter
name|adapter
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
specifier|final
name|CompilerSettings
name|settings
parameter_list|,
specifier|final
name|MethodWriter
name|adapter
parameter_list|)
block|{
for|for
control|(
specifier|final
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
name|settings
argument_list|,
name|adapter
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
operator|.
name|isStatic
argument_list|(
name|method
operator|.
name|reflect
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
name|adapter
operator|.
name|invokeStatic
argument_list|(
name|method
operator|.
name|owner
operator|.
name|type
argument_list|,
name|method
operator|.
name|method
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
operator|.
name|isInterface
argument_list|(
name|method
operator|.
name|owner
operator|.
name|clazz
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
name|adapter
operator|.
name|invokeInterface
argument_list|(
name|method
operator|.
name|owner
operator|.
name|type
argument_list|,
name|method
operator|.
name|method
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|adapter
operator|.
name|invokeVirtual
argument_list|(
name|method
operator|.
name|owner
operator|.
name|type
argument_list|,
name|method
operator|.
name|method
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|method
operator|.
name|rtn
operator|.
name|clazz
operator|.
name|equals
argument_list|(
name|method
operator|.
name|handle
operator|.
name|type
argument_list|()
operator|.
name|returnType
argument_list|()
argument_list|)
condition|)
block|{
name|adapter
operator|.
name|checkCast
argument_list|(
name|method
operator|.
name|rtn
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|store
name|void
name|store
parameter_list|(
specifier|final
name|CompilerSettings
name|settings
parameter_list|,
specifier|final
name|MethodWriter
name|adapter
parameter_list|)
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
block|}
end_class

end_unit

