begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.painless
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|painless
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
operator|.
name|Type
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

begin_comment
comment|/** Extension of locals for lambdas */
end_comment

begin_comment
comment|// Note: this isn't functional yet, it throws UOE
end_comment

begin_comment
comment|// TODO: implement slot renumbering for captures.
end_comment

begin_class
DECL|class|LambdaLocals
class|class
name|LambdaLocals
extends|extends
name|Locals
block|{
DECL|field|captures
specifier|private
name|List
argument_list|<
name|Variable
argument_list|>
name|captures
decl_stmt|;
DECL|method|LambdaLocals
name|LambdaLocals
parameter_list|(
name|Locals
name|parent
parameter_list|,
name|List
argument_list|<
name|Parameter
argument_list|>
name|parameters
parameter_list|,
name|List
argument_list|<
name|Variable
argument_list|>
name|captures
parameter_list|)
block|{
name|super
argument_list|(
name|parent
argument_list|)
expr_stmt|;
for|for
control|(
name|Parameter
name|parameter
range|:
name|parameters
control|)
block|{
name|defineVariable
argument_list|(
name|parameter
operator|.
name|location
argument_list|,
name|parameter
operator|.
name|type
argument_list|,
name|parameter
operator|.
name|name
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|captures
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|captures
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getVariable
specifier|public
name|Variable
name|getVariable
parameter_list|(
name|Location
name|location
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|Variable
name|variable
init|=
name|lookupVariable
argument_list|(
name|location
argument_list|,
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|variable
operator|!=
literal|null
condition|)
block|{
return|return
name|variable
return|;
block|}
if|if
condition|(
name|getParent
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|variable
operator|=
name|getParent
argument_list|()
operator|.
name|getVariable
argument_list|(
name|location
argument_list|,
name|name
argument_list|)
expr_stmt|;
if|if
condition|(
name|variable
operator|!=
literal|null
condition|)
block|{
assert|assert
name|captures
operator|!=
literal|null
assert|;
comment|// unused right now
comment|// make it read-only, and record that it was used.
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"lambda capture is not supported"
argument_list|)
throw|;
block|}
block|}
throw|throw
name|location
operator|.
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Variable ["
operator|+
name|name
operator|+
literal|"] is not defined."
argument_list|)
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|getReturnType
specifier|public
name|Type
name|getReturnType
parameter_list|()
block|{
return|return
name|Definition
operator|.
name|DEF_TYPE
return|;
block|}
block|}
end_class

end_unit

