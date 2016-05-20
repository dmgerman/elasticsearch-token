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
name|Definition
operator|.
name|Cast
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
name|AnalyzerCaster
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

begin_comment
comment|/**  * Represents a cast made in a variable/method chain.  */
end_comment

begin_class
DECL|class|LCast
specifier|public
specifier|final
class|class
name|LCast
extends|extends
name|ALink
block|{
DECL|field|type
specifier|final
name|String
name|type
decl_stmt|;
DECL|field|cast
name|Cast
name|cast
init|=
literal|null
decl_stmt|;
DECL|method|LCast
specifier|public
name|LCast
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
name|type
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
name|type
operator|=
name|type
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
if|if
condition|(
name|before
operator|==
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
elseif|else
if|if
condition|(
name|store
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Cannot assign a value to a cast."
argument_list|)
argument_list|)
throw|;
block|}
try|try
block|{
name|after
operator|=
name|Definition
operator|.
name|getType
argument_list|(
name|type
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
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Not a type ["
operator|+
name|type
operator|+
literal|"]."
argument_list|)
argument_list|)
throw|;
block|}
name|cast
operator|=
name|AnalyzerCaster
operator|.
name|getLegalCast
argument_list|(
name|location
argument_list|,
name|before
argument_list|,
name|after
argument_list|,
literal|true
argument_list|)
expr_stmt|;
return|return
name|cast
operator|!=
literal|null
condition|?
name|this
else|:
literal|null
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
name|adapter
operator|.
name|writeCast
argument_list|(
name|cast
argument_list|)
expr_stmt|;
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
comment|// Do nothing.
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

