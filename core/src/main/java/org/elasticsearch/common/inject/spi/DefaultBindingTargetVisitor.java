begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject.spi
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|spi
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
name|Binding
import|;
end_import

begin_comment
comment|/**  * No-op visitor for subclassing. All interface methods simply delegate to {@link  * #visitOther(Binding)}, returning its result.  *  * @param<V> any type to be returned by the visit method. Use {@link Void} with  *            {@code return null} if no return type is needed.  * @author jessewilson@google.com (Jesse Wilson)  * @since 2.0  */
end_comment

begin_class
DECL|class|DefaultBindingTargetVisitor
specifier|public
specifier|abstract
class|class
name|DefaultBindingTargetVisitor
parameter_list|<
name|T
parameter_list|,
name|V
parameter_list|>
implements|implements
name|BindingTargetVisitor
argument_list|<
name|T
argument_list|,
name|V
argument_list|>
block|{
comment|/**      * Default visit implementation. Returns {@code null}.      */
DECL|method|visitOther
specifier|protected
name|V
name|visitOther
parameter_list|(
name|Binding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|binding
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|InstanceBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|instanceBinding
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|instanceBinding
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|ProviderInstanceBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|providerInstanceBinding
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|providerInstanceBinding
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|ProviderKeyBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|providerKeyBinding
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|providerKeyBinding
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|LinkedKeyBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|linkedKeyBinding
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|linkedKeyBinding
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|ExposedBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|exposedBinding
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|exposedBinding
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|UntargettedBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|untargettedBinding
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|untargettedBinding
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|ConstructorBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|constructorBinding
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|constructorBinding
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|ConvertedConstantBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|convertedConstantBinding
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|convertedConstantBinding
argument_list|)
return|;
block|}
comment|// javac says it's an error to cast ProviderBinding<? extends T> to Binding<? extends T>
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|ProviderBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|providerBinding
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
operator|(
name|Binding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
operator|)
name|providerBinding
argument_list|)
return|;
block|}
block|}
end_class

end_unit

