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
name|DefBootstrap
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
name|FunctionRef
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
name|Variables
operator|.
name|Variable
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
name|Type
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|WriterConstants
operator|.
name|DEF_BOOTSTRAP_HANDLE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|WriterConstants
operator|.
name|LAMBDA_BOOTSTRAP_HANDLE
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|invoke
operator|.
name|LambdaMetafactory
import|;
end_import

begin_comment
comment|/**  * Represents a capturing function reference.  */
end_comment

begin_class
DECL|class|ECapturingFunctionRef
specifier|public
class|class
name|ECapturingFunctionRef
extends|extends
name|AExpression
block|{
DECL|field|type
specifier|public
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|call
specifier|public
specifier|final
name|String
name|call
decl_stmt|;
DECL|field|ref
specifier|private
name|FunctionRef
name|ref
decl_stmt|;
DECL|field|captured
name|Variable
name|captured
decl_stmt|;
DECL|field|defInterface
specifier|private
name|boolean
name|defInterface
decl_stmt|;
DECL|method|ECapturingFunctionRef
specifier|public
name|ECapturingFunctionRef
parameter_list|(
name|Location
name|location
parameter_list|,
name|String
name|type
parameter_list|,
name|String
name|call
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
name|type
expr_stmt|;
name|this
operator|.
name|call
operator|=
name|call
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
name|captured
operator|=
name|variables
operator|.
name|getVariable
argument_list|(
name|location
argument_list|,
name|type
argument_list|)
expr_stmt|;
if|if
condition|(
name|expected
operator|==
literal|null
condition|)
block|{
name|defInterface
operator|=
literal|true
expr_stmt|;
name|actual
operator|=
name|Definition
operator|.
name|getType
argument_list|(
literal|"String"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|defInterface
operator|=
literal|false
expr_stmt|;
comment|// static case
if|if
condition|(
name|captured
operator|.
name|type
operator|.
name|sort
operator|!=
name|Definition
operator|.
name|Sort
operator|.
name|DEF
condition|)
block|{
try|try
block|{
name|ref
operator|=
operator|new
name|FunctionRef
argument_list|(
name|expected
argument_list|,
name|captured
operator|.
name|type
operator|.
name|name
argument_list|,
name|call
argument_list|,
name|captured
operator|.
name|type
operator|.
name|clazz
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
throw|throw
name|createError
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
name|actual
operator|=
name|expected
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
name|writer
operator|.
name|writeDebugInfo
argument_list|(
name|location
argument_list|)
expr_stmt|;
if|if
condition|(
name|defInterface
operator|&&
name|captured
operator|.
name|type
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
comment|// dynamic interface, dynamic implementation
name|writer
operator|.
name|push
argument_list|(
literal|"D"
operator|+
name|type
operator|+
literal|"."
operator|+
name|call
operator|+
literal|",1"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|loadLocal
argument_list|(
name|captured
operator|.
name|slot
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|defInterface
condition|)
block|{
comment|// dynamic interface, typed implementation
name|writer
operator|.
name|push
argument_list|(
literal|"S"
operator|+
name|captured
operator|.
name|type
operator|.
name|name
operator|+
literal|"."
operator|+
name|call
operator|+
literal|",1"
argument_list|)
expr_stmt|;
name|writer
operator|.
name|loadLocal
argument_list|(
name|captured
operator|.
name|slot
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ref
operator|==
literal|null
condition|)
block|{
comment|// typed interface, dynamic implementation
name|writer
operator|.
name|loadLocal
argument_list|(
name|captured
operator|.
name|slot
argument_list|)
expr_stmt|;
name|String
name|descriptor
init|=
name|Type
operator|.
name|getMethodType
argument_list|(
name|expected
operator|.
name|type
argument_list|,
name|captured
operator|.
name|type
operator|.
name|type
argument_list|)
operator|.
name|getDescriptor
argument_list|()
decl_stmt|;
name|writer
operator|.
name|invokeDynamic
argument_list|(
name|call
argument_list|,
name|descriptor
argument_list|,
name|DEF_BOOTSTRAP_HANDLE
argument_list|,
operator|(
name|Object
operator|)
name|DefBootstrap
operator|.
name|REFERENCE
argument_list|,
name|expected
operator|.
name|name
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// typed interface, typed implementation
name|writer
operator|.
name|loadLocal
argument_list|(
name|captured
operator|.
name|slot
argument_list|)
expr_stmt|;
comment|// convert MethodTypes to asm Type for the constant pool.
name|String
name|invokedType
init|=
name|ref
operator|.
name|invokedType
operator|.
name|toMethodDescriptorString
argument_list|()
decl_stmt|;
name|Type
name|samMethodType
init|=
name|Type
operator|.
name|getMethodType
argument_list|(
name|ref
operator|.
name|samMethodType
operator|.
name|toMethodDescriptorString
argument_list|()
argument_list|)
decl_stmt|;
name|Type
name|interfaceType
init|=
name|Type
operator|.
name|getMethodType
argument_list|(
name|ref
operator|.
name|interfaceMethodType
operator|.
name|toMethodDescriptorString
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|ref
operator|.
name|needsBridges
argument_list|()
condition|)
block|{
name|writer
operator|.
name|invokeDynamic
argument_list|(
name|ref
operator|.
name|invokedName
argument_list|,
name|invokedType
argument_list|,
name|LAMBDA_BOOTSTRAP_HANDLE
argument_list|,
name|samMethodType
argument_list|,
name|ref
operator|.
name|implMethodASM
argument_list|,
name|samMethodType
argument_list|,
name|LambdaMetafactory
operator|.
name|FLAG_BRIDGES
argument_list|,
literal|1
argument_list|,
name|interfaceType
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|writer
operator|.
name|invokeDynamic
argument_list|(
name|ref
operator|.
name|invokedName
argument_list|,
name|invokedType
argument_list|,
name|LAMBDA_BOOTSTRAP_HANDLE
argument_list|,
name|samMethodType
argument_list|,
name|ref
operator|.
name|implMethodASM
argument_list|,
name|samMethodType
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

