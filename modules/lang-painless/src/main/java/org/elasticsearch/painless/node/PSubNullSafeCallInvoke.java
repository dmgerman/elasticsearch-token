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
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Label
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

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Objects
operator|.
name|requireNonNull
import|;
end_import

begin_comment
comment|/**  * Implements a call who's value is null if the prefix is null rather than throwing an NPE.  */
end_comment

begin_class
DECL|class|PSubNullSafeCallInvoke
specifier|public
class|class
name|PSubNullSafeCallInvoke
extends|extends
name|AExpression
block|{
comment|/**      * The expression gaurded by the null check. Required at construction time and replaced at analysis time.      */
DECL|field|guarded
specifier|private
name|AExpression
name|guarded
decl_stmt|;
DECL|method|PSubNullSafeCallInvoke
specifier|public
name|PSubNullSafeCallInvoke
parameter_list|(
name|Location
name|location
parameter_list|,
name|AExpression
name|guarded
parameter_list|)
block|{
name|super
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|guarded
operator|=
name|requireNonNull
argument_list|(
name|guarded
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
name|guarded
operator|.
name|extractVariables
argument_list|(
name|variables
argument_list|)
expr_stmt|;
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
name|guarded
operator|.
name|analyze
argument_list|(
name|locals
argument_list|)
expr_stmt|;
name|actual
operator|=
name|guarded
operator|.
name|actual
expr_stmt|;
if|if
condition|(
name|actual
operator|.
name|sort
operator|.
name|primitive
condition|)
block|{
comment|// Result must be nullable. We emit boxing instructions if needed.
name|actual
operator|=
name|Definition
operator|.
name|getType
argument_list|(
name|actual
operator|.
name|sort
operator|.
name|boxed
operator|.
name|getSimpleName
argument_list|()
argument_list|)
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
name|Label
name|end
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
name|writer
operator|.
name|dup
argument_list|()
expr_stmt|;
name|writer
operator|.
name|ifNull
argument_list|(
name|end
argument_list|)
expr_stmt|;
name|guarded
operator|.
name|write
argument_list|(
name|writer
argument_list|,
name|globals
argument_list|)
expr_stmt|;
if|if
condition|(
name|guarded
operator|.
name|actual
operator|.
name|sort
operator|.
name|primitive
condition|)
block|{
comment|// Box primitives so they are nullable
name|writer
operator|.
name|box
argument_list|(
name|guarded
operator|.
name|actual
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|mark
argument_list|(
name|end
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
