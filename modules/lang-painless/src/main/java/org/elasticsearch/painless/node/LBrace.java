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
name|Sort
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
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Represents an array load/store or defers to possible shortcuts.  */
end_comment

begin_class
DECL|class|LBrace
specifier|public
specifier|final
class|class
name|LBrace
extends|extends
name|ALink
block|{
DECL|field|index
name|AExpression
name|index
decl_stmt|;
DECL|method|LBrace
specifier|public
name|LBrace
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
name|AExpression
name|index
parameter_list|)
block|{
name|super
argument_list|(
name|line
argument_list|,
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
specifier|final
name|CompilerSettings
name|settings
parameter_list|,
specifier|final
name|Definition
name|definition
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
specifier|final
name|Sort
name|sort
init|=
name|before
operator|.
name|sort
decl_stmt|;
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|ARRAY
condition|)
block|{
name|index
operator|.
name|expected
operator|=
name|Definition
operator|.
name|intType
expr_stmt|;
name|index
operator|.
name|analyze
argument_list|(
name|settings
argument_list|,
name|definition
argument_list|,
name|variables
argument_list|)
expr_stmt|;
name|index
operator|=
name|index
operator|.
name|cast
argument_list|(
name|settings
argument_list|,
name|definition
argument_list|,
name|variables
argument_list|)
expr_stmt|;
name|after
operator|=
name|Definition
operator|.
name|getType
argument_list|(
name|before
operator|.
name|struct
argument_list|,
name|before
operator|.
name|dimensions
operator|-
literal|1
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
elseif|else
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|DEF
condition|)
block|{
return|return
operator|new
name|LDefArray
argument_list|(
name|line
argument_list|,
name|location
argument_list|,
name|index
argument_list|)
operator|.
name|copy
argument_list|(
name|this
argument_list|)
operator|.
name|analyze
argument_list|(
name|settings
argument_list|,
name|definition
argument_list|,
name|variables
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|Map
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|before
operator|.
name|clazz
argument_list|)
condition|)
block|{
return|return
operator|new
name|LMapShortcut
argument_list|(
name|line
argument_list|,
name|location
argument_list|,
name|index
argument_list|)
operator|.
name|copy
argument_list|(
name|this
argument_list|)
operator|.
name|analyze
argument_list|(
name|settings
argument_list|,
name|definition
argument_list|,
name|variables
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|List
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|before
operator|.
name|clazz
argument_list|)
condition|)
block|{
return|return
operator|new
name|LListShortcut
argument_list|(
name|line
argument_list|,
name|location
argument_list|,
name|index
argument_list|)
operator|.
name|copy
argument_list|(
name|this
argument_list|)
operator|.
name|analyze
argument_list|(
name|settings
argument_list|,
name|definition
argument_list|,
name|variables
argument_list|)
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Illegal array access on type ["
operator|+
name|before
operator|.
name|name
operator|+
literal|"]."
argument_list|)
argument_list|)
throw|;
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
name|Definition
name|definition
parameter_list|,
specifier|final
name|MethodWriter
name|adapter
parameter_list|)
block|{
name|index
operator|.
name|write
argument_list|(
name|settings
argument_list|,
name|definition
argument_list|,
name|adapter
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
name|Definition
name|definition
parameter_list|,
specifier|final
name|MethodWriter
name|adapter
parameter_list|)
block|{
name|adapter
operator|.
name|arrayLoad
argument_list|(
name|after
operator|.
name|type
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
name|Definition
name|definition
parameter_list|,
specifier|final
name|MethodWriter
name|adapter
parameter_list|)
block|{
name|adapter
operator|.
name|arrayStore
argument_list|(
name|after
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

