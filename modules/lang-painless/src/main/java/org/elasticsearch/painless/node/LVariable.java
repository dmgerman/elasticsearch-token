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
name|Opcodes
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

begin_comment
comment|/**  * Represents a variable load/store.  */
end_comment

begin_class
DECL|class|LVariable
specifier|public
specifier|final
class|class
name|LVariable
extends|extends
name|ALink
block|{
DECL|field|name
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|slot
name|int
name|slot
decl_stmt|;
DECL|method|LVariable
specifier|public
name|LVariable
parameter_list|(
name|int
name|line
parameter_list|,
name|String
name|location
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|line
argument_list|,
name|location
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|analyze
name|ALink
name|analyze
parameter_list|(
name|Variables
name|variables
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
name|Type
name|type
init|=
literal|null
decl_stmt|;
try|try
block|{
name|type
operator|=
name|Definition
operator|.
name|getType
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
specifier|final
name|IllegalArgumentException
name|exception
parameter_list|)
block|{
comment|// Do nothing.
block|}
if|if
condition|(
name|type
operator|!=
literal|null
condition|)
block|{
name|statik
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
specifier|final
name|Variable
name|variable
init|=
name|variables
operator|.
name|getVariable
argument_list|(
name|location
argument_list|,
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|store
operator|&&
name|variable
operator|.
name|readonly
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Variable ["
operator|+
name|variable
operator|.
name|name
operator|+
literal|"] is read-only."
argument_list|)
argument_list|)
throw|;
block|}
name|slot
operator|=
name|variable
operator|.
name|slot
expr_stmt|;
name|after
operator|=
name|variable
operator|.
name|type
expr_stmt|;
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
name|MethodWriter
name|adapter
parameter_list|)
block|{
name|adapter
operator|.
name|visitVarInsn
argument_list|(
name|after
operator|.
name|type
operator|.
name|getOpcode
argument_list|(
name|Opcodes
operator|.
name|ILOAD
argument_list|)
argument_list|,
name|slot
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
name|adapter
parameter_list|)
block|{
name|adapter
operator|.
name|visitVarInsn
argument_list|(
name|after
operator|.
name|type
operator|.
name|getOpcode
argument_list|(
name|Opcodes
operator|.
name|ISTORE
argument_list|)
argument_list|,
name|slot
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

