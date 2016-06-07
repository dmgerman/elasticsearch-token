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
name|MethodWriter
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

begin_comment
comment|/**  * Represents an array load/store or shortcut on a def type.  (Internal only.)  */
end_comment

begin_class
DECL|class|LDefArray
specifier|final
class|class
name|LDefArray
extends|extends
name|ALink
implements|implements
name|IDefLink
block|{
DECL|field|index
name|AExpression
name|index
decl_stmt|;
DECL|method|LDefArray
name|LDefArray
parameter_list|(
name|Location
name|location
parameter_list|,
name|AExpression
name|index
parameter_list|)
block|{
name|super
argument_list|(
name|location
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|this
operator|.
name|index
operator|=
name|index
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
name|index
operator|.
name|analyze
argument_list|(
name|variables
argument_list|)
expr_stmt|;
name|index
operator|.
name|expected
operator|=
name|index
operator|.
name|actual
expr_stmt|;
name|index
operator|=
name|index
operator|.
name|cast
argument_list|(
name|variables
argument_list|)
expr_stmt|;
name|after
operator|=
name|Definition
operator|.
name|DEF_TYPE
expr_stmt|;
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
name|index
operator|.
name|write
argument_list|(
name|writer
argument_list|)
expr_stmt|;
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
name|String
name|desc
init|=
name|Type
operator|.
name|getMethodDescriptor
argument_list|(
name|after
operator|.
name|type
argument_list|,
name|Definition
operator|.
name|DEF_TYPE
operator|.
name|type
argument_list|,
name|index
operator|.
name|actual
operator|.
name|type
argument_list|)
decl_stmt|;
name|writer
operator|.
name|invokeDynamic
argument_list|(
literal|"arrayLoad"
argument_list|,
name|desc
argument_list|,
name|DEF_BOOTSTRAP_HANDLE
argument_list|,
operator|(
name|Object
operator|)
name|DefBootstrap
operator|.
name|ARRAY_LOAD
argument_list|,
literal|0
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
name|writer
operator|.
name|writeDebugInfo
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|String
name|desc
init|=
name|Type
operator|.
name|getMethodDescriptor
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
name|index
operator|.
name|actual
operator|.
name|type
argument_list|,
name|after
operator|.
name|type
argument_list|)
decl_stmt|;
name|writer
operator|.
name|invokeDynamic
argument_list|(
literal|"arrayStore"
argument_list|,
name|desc
argument_list|,
name|DEF_BOOTSTRAP_HANDLE
argument_list|,
operator|(
name|Object
operator|)
name|DefBootstrap
operator|.
name|ARRAY_STORE
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

