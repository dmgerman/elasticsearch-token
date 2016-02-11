begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2007 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject.assistedinject
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|assistedinject
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|TypeLiteral
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_comment
comment|/**  * A list of {@link TypeLiteral}s to match an injectable Constructor's assisted  * parameter types to the corresponding factory method.  *  * @author jmourits@google.com (Jerome Mourits)  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|ParameterListKey
class|class
name|ParameterListKey
block|{
DECL|field|paramList
specifier|private
specifier|final
name|List
argument_list|<
name|Type
argument_list|>
name|paramList
decl_stmt|;
DECL|method|ParameterListKey
specifier|public
name|ParameterListKey
parameter_list|(
name|List
argument_list|<
name|Type
argument_list|>
name|paramList
parameter_list|)
block|{
name|this
operator|.
name|paramList
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|paramList
argument_list|)
expr_stmt|;
block|}
DECL|method|ParameterListKey
specifier|public
name|ParameterListKey
parameter_list|(
name|Type
index|[]
name|types
parameter_list|)
block|{
name|this
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|types
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|ParameterListKey
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
name|ParameterListKey
name|other
init|=
operator|(
name|ParameterListKey
operator|)
name|o
decl_stmt|;
return|return
name|paramList
operator|.
name|equals
argument_list|(
name|other
operator|.
name|paramList
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|paramList
operator|.
name|hashCode
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|paramList
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

