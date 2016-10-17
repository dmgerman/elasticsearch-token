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
comment|/**  * Represents a field load/store or shortcut on a def type.  (Internal only.)  */
end_comment

begin_class
DECL|class|PSubDefField
specifier|final
class|class
name|PSubDefField
extends|extends
name|AStoreable
block|{
DECL|field|value
specifier|private
specifier|final
name|String
name|value
decl_stmt|;
DECL|method|PSubDefField
name|PSubDefField
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
name|actual
operator|=
name|expected
operator|==
literal|null
operator|||
name|explicit
condition|?
name|Definition
operator|.
name|DEF_TYPE
else|:
name|expected
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
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
name|methodType
init|=
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
operator|.
name|getMethodType
argument_list|(
name|actual
operator|.
name|type
argument_list|,
name|Definition
operator|.
name|DEF_TYPE
operator|.
name|type
argument_list|)
decl_stmt|;
name|writer
operator|.
name|invokeDefCall
argument_list|(
name|value
argument_list|,
name|methodType
argument_list|,
name|DefBootstrap
operator|.
name|LOAD
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|accessElementCount
name|int
name|accessElementCount
parameter_list|()
block|{
return|return
literal|1
return|;
block|}
annotation|@
name|Override
DECL|method|isDefOptimized
name|boolean
name|isDefOptimized
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|updateActual
name|void
name|updateActual
parameter_list|(
name|Type
name|actual
parameter_list|)
block|{
name|this
operator|.
name|actual
operator|=
name|actual
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setup
name|void
name|setup
parameter_list|(
name|MethodWriter
name|writer
parameter_list|,
name|Globals
name|globals
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
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
name|methodType
init|=
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
operator|.
name|getMethodType
argument_list|(
name|actual
operator|.
name|type
argument_list|,
name|Definition
operator|.
name|DEF_TYPE
operator|.
name|type
argument_list|)
decl_stmt|;
name|writer
operator|.
name|invokeDefCall
argument_list|(
name|value
argument_list|,
name|methodType
argument_list|,
name|DefBootstrap
operator|.
name|LOAD
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
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
name|methodType
init|=
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
operator|.
name|getMethodType
argument_list|(
name|Definition
operator|.
name|VOID_TYPE
operator|.
name|type
argument_list|,
name|Definition
operator|.
name|DEF_TYPE
operator|.
name|type
argument_list|,
name|actual
operator|.
name|type
argument_list|)
decl_stmt|;
name|writer
operator|.
name|invokeDefCall
argument_list|(
name|value
argument_list|,
name|methodType
argument_list|,
name|DefBootstrap
operator|.
name|STORE
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
