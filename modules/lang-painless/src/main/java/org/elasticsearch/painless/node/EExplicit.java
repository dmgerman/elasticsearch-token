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
comment|/**  * Represents an explicit cast.  */
end_comment

begin_class
DECL|class|EExplicit
specifier|public
specifier|final
class|class
name|EExplicit
extends|extends
name|AExpression
block|{
DECL|field|type
specifier|private
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|child
specifier|private
name|AExpression
name|child
decl_stmt|;
DECL|method|EExplicit
specifier|public
name|EExplicit
parameter_list|(
name|Location
name|location
parameter_list|,
name|String
name|type
parameter_list|,
name|AExpression
name|child
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
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|type
argument_list|)
expr_stmt|;
name|this
operator|.
name|child
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|child
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
name|child
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
try|try
block|{
name|actual
operator|=
name|Definition
operator|.
name|getType
argument_list|(
name|this
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|exception
parameter_list|)
block|{
throw|throw
name|createError
argument_list|(
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Not a type ["
operator|+
name|this
operator|.
name|type
operator|+
literal|"]."
argument_list|)
argument_list|)
throw|;
block|}
name|child
operator|.
name|expected
operator|=
name|actual
expr_stmt|;
name|child
operator|.
name|explicit
operator|=
literal|true
expr_stmt|;
name|child
operator|.
name|analyze
argument_list|(
name|locals
argument_list|)
expr_stmt|;
name|child
operator|=
name|child
operator|.
name|cast
argument_list|(
name|locals
argument_list|)
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
DECL|method|cast
name|AExpression
name|cast
parameter_list|(
name|Locals
name|locals
parameter_list|)
block|{
name|child
operator|.
name|expected
operator|=
name|expected
expr_stmt|;
name|child
operator|.
name|explicit
operator|=
name|explicit
expr_stmt|;
name|child
operator|.
name|internal
operator|=
name|internal
expr_stmt|;
return|return
name|child
operator|.
name|cast
argument_list|(
name|locals
argument_list|)
return|;
block|}
block|}
end_class

end_unit

