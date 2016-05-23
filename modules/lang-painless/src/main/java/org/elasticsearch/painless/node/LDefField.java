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
comment|/**  * Represents a field load/store or shortcut on a def type.  (Internal only.)  */
end_comment

begin_class
DECL|class|LDefField
specifier|final
class|class
name|LDefField
extends|extends
name|ALink
implements|implements
name|IDefLink
block|{
DECL|field|value
specifier|final
name|String
name|value
decl_stmt|;
DECL|method|LDefField
name|LDefField
parameter_list|(
name|int
name|line
parameter_list|,
name|int
name|offset
parameter_list|,
name|String
name|location
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|line
argument_list|,
name|offset
argument_list|,
name|location
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
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
argument_list|)
decl_stmt|;
name|writer
operator|.
name|invokeDynamic
argument_list|(
name|value
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
parameter_list|)
block|{
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
name|after
operator|.
name|type
argument_list|)
decl_stmt|;
name|writer
operator|.
name|invokeDynamic
argument_list|(
name|value
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
name|STORE
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

