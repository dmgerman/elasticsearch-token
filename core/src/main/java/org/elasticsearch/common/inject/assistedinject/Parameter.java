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
name|BindingAnnotation
import|;
end_import

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
name|ConfigurationException
import|;
end_import

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
name|Injector
import|;
end_import

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
name|Key
import|;
end_import

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
name|Provider
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Annotation
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
name|ParameterizedType
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

begin_comment
comment|/**  * Models a method or constructor parameter.  *  * @author jmourits@google.com (Jerome Mourits)  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|Parameter
class|class
name|Parameter
block|{
DECL|field|type
specifier|private
specifier|final
name|Type
name|type
decl_stmt|;
DECL|field|isAssisted
specifier|private
specifier|final
name|boolean
name|isAssisted
decl_stmt|;
DECL|field|bindingAnnotation
specifier|private
specifier|final
name|Annotation
name|bindingAnnotation
decl_stmt|;
DECL|field|isProvider
specifier|private
specifier|final
name|boolean
name|isProvider
decl_stmt|;
DECL|method|Parameter
name|Parameter
parameter_list|(
name|Type
name|type
parameter_list|,
name|Annotation
index|[]
name|annotations
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
name|this
operator|.
name|bindingAnnotation
operator|=
name|getBindingAnnotation
argument_list|(
name|annotations
argument_list|)
expr_stmt|;
name|this
operator|.
name|isAssisted
operator|=
name|hasAssistedAnnotation
argument_list|(
name|annotations
argument_list|)
expr_stmt|;
name|this
operator|.
name|isProvider
operator|=
name|isProvider
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
DECL|method|isProvidedByFactory
specifier|public
name|boolean
name|isProvidedByFactory
parameter_list|()
block|{
return|return
name|isAssisted
return|;
block|}
DECL|method|getType
specifier|public
name|Type
name|getType
parameter_list|()
block|{
return|return
name|type
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
name|StringBuilder
name|result
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|isAssisted
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
literal|"@Assisted"
argument_list|)
expr_stmt|;
name|result
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|bindingAnnotation
operator|!=
literal|null
condition|)
block|{
name|result
operator|.
name|append
argument_list|(
name|bindingAnnotation
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|result
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
name|result
operator|.
name|append
argument_list|(
name|type
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|hasAssistedAnnotation
specifier|private
name|boolean
name|hasAssistedAnnotation
parameter_list|(
name|Annotation
index|[]
name|annotations
parameter_list|)
block|{
for|for
control|(
name|Annotation
name|annotation
range|:
name|annotations
control|)
block|{
if|if
condition|(
name|annotation
operator|.
name|annotationType
argument_list|()
operator|.
name|equals
argument_list|(
name|Assisted
operator|.
name|class
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
block|}
comment|/**      * Returns the Guice {@link Key} for this parameter.      */
DECL|method|getValue
specifier|public
name|Object
name|getValue
parameter_list|(
name|Injector
name|injector
parameter_list|)
block|{
return|return
name|isProvider
condition|?
name|injector
operator|.
name|getProvider
argument_list|(
name|getBindingForType
argument_list|(
name|getProvidedType
argument_list|(
name|type
argument_list|)
argument_list|)
argument_list|)
else|:
name|injector
operator|.
name|getInstance
argument_list|(
name|getPrimaryBindingKey
argument_list|()
argument_list|)
return|;
block|}
DECL|method|getPrimaryBindingKey
name|Key
argument_list|<
name|?
argument_list|>
name|getPrimaryBindingKey
parameter_list|()
block|{
return|return
name|isProvider
condition|?
name|getBindingForType
argument_list|(
name|getProvidedType
argument_list|(
name|type
argument_list|)
argument_list|)
else|:
name|getBindingForType
argument_list|(
name|type
argument_list|)
return|;
block|}
DECL|method|getProvidedType
specifier|private
name|Type
name|getProvidedType
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
return|return
operator|(
operator|(
name|ParameterizedType
operator|)
name|type
operator|)
operator|.
name|getActualTypeArguments
argument_list|()
index|[
literal|0
index|]
return|;
block|}
DECL|method|isProvider
specifier|private
name|boolean
name|isProvider
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
return|return
name|type
operator|instanceof
name|ParameterizedType
operator|&&
operator|(
operator|(
name|ParameterizedType
operator|)
name|type
operator|)
operator|.
name|getRawType
argument_list|()
operator|==
name|Provider
operator|.
name|class
return|;
block|}
DECL|method|getBindingForType
specifier|private
name|Key
argument_list|<
name|?
argument_list|>
name|getBindingForType
parameter_list|(
name|Type
name|type
parameter_list|)
block|{
return|return
name|bindingAnnotation
operator|!=
literal|null
condition|?
name|Key
operator|.
name|get
argument_list|(
name|type
argument_list|,
name|bindingAnnotation
argument_list|)
else|:
name|Key
operator|.
name|get
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**      * Returns the unique binding annotation from the specified list, or      * {@code null} if there are none.      *      * @throws IllegalStateException if multiple binding annotations exist.      */
DECL|method|getBindingAnnotation
specifier|private
name|Annotation
name|getBindingAnnotation
parameter_list|(
name|Annotation
index|[]
name|annotations
parameter_list|)
block|{
name|Annotation
name|bindingAnnotation
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Annotation
name|a
range|:
name|annotations
control|)
block|{
if|if
condition|(
name|a
operator|.
name|annotationType
argument_list|()
operator|.
name|getAnnotation
argument_list|(
name|BindingAnnotation
operator|.
name|class
argument_list|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|bindingAnnotation
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Parameter has multiple binding annotations: "
operator|+
name|bindingAnnotation
operator|+
literal|" and "
operator|+
name|a
argument_list|)
throw|;
block|}
name|bindingAnnotation
operator|=
name|a
expr_stmt|;
block|}
block|}
return|return
name|bindingAnnotation
return|;
block|}
block|}
end_class

end_unit

