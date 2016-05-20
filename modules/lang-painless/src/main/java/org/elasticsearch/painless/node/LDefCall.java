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
comment|/**  * Represents a method call made on a def type. (Internal only.)  */
end_comment

begin_class
DECL|class|LDefCall
specifier|final
class|class
name|LDefCall
extends|extends
name|ALink
implements|implements
name|IDefLink
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
DECL|method|LDefCall
name|LDefCall
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
name|analyze
argument_list|(
name|settings
argument_list|,
name|variables
argument_list|)
expr_stmt|;
name|expression
operator|.
name|expected
operator|=
name|expression
operator|.
name|actual
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
name|Definition
operator|.
name|defType
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
specifier|final
name|StringBuilder
name|signature
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|signature
operator|.
name|append
argument_list|(
literal|'('
argument_list|)
expr_stmt|;
comment|// first parameter is the receiver, we never know its type: always Object
name|signature
operator|.
name|append
argument_list|(
name|Definition
operator|.
name|defType
operator|.
name|type
operator|.
name|getDescriptor
argument_list|()
argument_list|)
expr_stmt|;
comment|// TODO: remove our explicit conversions and feed more type information for return value,
comment|// it can avoid some unnecessary boxing etc.
for|for
control|(
specifier|final
name|AExpression
name|argument
range|:
name|arguments
control|)
block|{
name|signature
operator|.
name|append
argument_list|(
name|argument
operator|.
name|actual
operator|.
name|type
operator|.
name|getDescriptor
argument_list|()
argument_list|)
expr_stmt|;
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
name|signature
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
expr_stmt|;
comment|// return value
name|signature
operator|.
name|append
argument_list|(
name|after
operator|.
name|type
operator|.
name|getDescriptor
argument_list|()
argument_list|)
expr_stmt|;
name|adapter
operator|.
name|invokeDynamic
argument_list|(
name|name
argument_list|,
name|signature
operator|.
name|toString
argument_list|()
argument_list|,
name|DEF_BOOTSTRAP_HANDLE
argument_list|,
name|DefBootstrap
operator|.
name|METHOD_CALL
argument_list|)
expr_stmt|;
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

