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

begin_comment
comment|/**  * Represents a map load/store shortcut. (Internal only.)  */
end_comment

begin_class
DECL|class|LMapShortcut
specifier|final
class|class
name|LMapShortcut
extends|extends
name|ALink
block|{
DECL|field|index
name|AExpression
name|index
decl_stmt|;
DECL|field|getter
name|Method
name|getter
decl_stmt|;
DECL|field|setter
name|Method
name|setter
decl_stmt|;
DECL|method|LMapShortcut
name|LMapShortcut
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
name|AExpression
name|index
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
name|getter
operator|=
name|before
operator|.
name|struct
operator|.
name|methods
operator|.
name|get
argument_list|(
operator|new
name|Definition
operator|.
name|MethodKey
argument_list|(
literal|"get"
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|setter
operator|=
name|before
operator|.
name|struct
operator|.
name|methods
operator|.
name|get
argument_list|(
operator|new
name|Definition
operator|.
name|MethodKey
argument_list|(
literal|"put"
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|getter
operator|!=
literal|null
operator|&&
operator|(
name|getter
operator|.
name|rtn
operator|.
name|sort
operator|==
name|Sort
operator|.
name|VOID
operator|||
name|getter
operator|.
name|arguments
operator|.
name|size
argument_list|()
operator|!=
literal|1
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Illegal map get shortcut for type ["
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
if|if
condition|(
name|setter
operator|!=
literal|null
operator|&&
name|setter
operator|.
name|arguments
operator|.
name|size
argument_list|()
operator|!=
literal|2
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Illegal map set shortcut for type ["
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
if|if
condition|(
name|getter
operator|!=
literal|null
operator|&&
name|setter
operator|!=
literal|null
operator|&&
operator|(
operator|!
name|getter
operator|.
name|arguments
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|equals
argument_list|(
name|setter
operator|.
name|arguments
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|||
operator|!
name|getter
operator|.
name|rtn
operator|.
name|equals
argument_list|(
name|setter
operator|.
name|arguments
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Shortcut argument types must match."
argument_list|)
argument_list|)
throw|;
block|}
if|if
condition|(
operator|(
name|load
operator|||
name|store
operator|)
operator|&&
operator|(
operator|!
name|load
operator|||
name|getter
operator|!=
literal|null
operator|)
operator|&&
operator|(
operator|!
name|store
operator|||
name|setter
operator|!=
literal|null
operator|)
condition|)
block|{
name|index
operator|.
name|expected
operator|=
name|setter
operator|!=
literal|null
condition|?
name|setter
operator|.
name|arguments
operator|.
name|get
argument_list|(
literal|0
argument_list|)
else|:
name|getter
operator|.
name|arguments
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|index
operator|.
name|analyze
argument_list|(
name|variables
argument_list|)
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
name|setter
operator|!=
literal|null
condition|?
name|setter
operator|.
name|arguments
operator|.
name|get
argument_list|(
literal|1
argument_list|)
else|:
name|getter
operator|.
name|rtn
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
name|error
argument_list|(
literal|"Illegal map shortcut for type ["
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
name|offset
argument_list|)
expr_stmt|;
if|if
condition|(
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
operator|.
name|isInterface
argument_list|(
name|getter
operator|.
name|owner
operator|.
name|clazz
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
name|writer
operator|.
name|invokeInterface
argument_list|(
name|getter
operator|.
name|owner
operator|.
name|type
argument_list|,
name|getter
operator|.
name|method
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|writer
operator|.
name|invokeVirtual
argument_list|(
name|getter
operator|.
name|owner
operator|.
name|type
argument_list|,
name|getter
operator|.
name|method
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|getter
operator|.
name|rtn
operator|.
name|clazz
operator|.
name|equals
argument_list|(
name|getter
operator|.
name|handle
operator|.
name|type
argument_list|()
operator|.
name|returnType
argument_list|()
argument_list|)
condition|)
block|{
name|writer
operator|.
name|checkCast
argument_list|(
name|getter
operator|.
name|rtn
operator|.
name|type
argument_list|)
expr_stmt|;
block|}
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
name|offset
argument_list|)
expr_stmt|;
if|if
condition|(
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Modifier
operator|.
name|isInterface
argument_list|(
name|setter
operator|.
name|owner
operator|.
name|clazz
operator|.
name|getModifiers
argument_list|()
argument_list|)
condition|)
block|{
name|writer
operator|.
name|invokeInterface
argument_list|(
name|setter
operator|.
name|owner
operator|.
name|type
argument_list|,
name|setter
operator|.
name|method
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|writer
operator|.
name|invokeVirtual
argument_list|(
name|setter
operator|.
name|owner
operator|.
name|type
argument_list|,
name|setter
operator|.
name|method
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|writePop
argument_list|(
name|setter
operator|.
name|rtn
operator|.
name|sort
operator|.
name|size
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

