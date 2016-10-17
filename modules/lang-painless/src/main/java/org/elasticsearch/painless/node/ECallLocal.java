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
name|MethodKey
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
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|WriterConstants
operator|.
name|CLASS_TYPE
import|;
end_import

begin_comment
comment|/**  * Represents a user-defined call.  */
end_comment

begin_class
DECL|class|ECallLocal
specifier|public
specifier|final
class|class
name|ECallLocal
extends|extends
name|AExpression
block|{
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|arguments
specifier|private
specifier|final
name|List
argument_list|<
name|AExpression
argument_list|>
name|arguments
decl_stmt|;
DECL|field|method
specifier|private
name|Method
name|method
init|=
literal|null
decl_stmt|;
DECL|method|ECallLocal
specifier|public
name|ECallLocal
parameter_list|(
name|Location
name|location
parameter_list|,
name|String
name|name
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
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|arguments
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|arguments
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
name|extractVariables
argument_list|(
name|variables
argument_list|)
expr_stmt|;
block|}
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
name|MethodKey
name|methodKey
init|=
operator|new
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
name|method
operator|=
name|locals
operator|.
name|getMethod
argument_list|(
name|methodKey
argument_list|)
expr_stmt|;
if|if
condition|(
name|method
operator|==
literal|null
condition|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
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
literal|"] arguments."
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
name|actual
operator|=
name|method
operator|.
name|rtn
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
argument_list|,
name|globals
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|invokeStatic
argument_list|(
name|CLASS_TYPE
argument_list|,
name|method
operator|.
name|method
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
