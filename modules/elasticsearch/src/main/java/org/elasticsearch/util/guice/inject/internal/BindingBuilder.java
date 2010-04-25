begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.guice.inject.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|internal
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|Binder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
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
name|util
operator|.
name|guice
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
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|Provider
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|TypeLiteral
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|binder
operator|.
name|AnnotatedBindingBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|internal
operator|.
name|Preconditions
operator|.
name|checkNotNull
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|spi
operator|.
name|Element
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|spi
operator|.
name|InjectionPoint
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|spi
operator|.
name|Message
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|ImmutableSet
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
name|Set
import|;
end_import

begin_comment
comment|/**  * Bind a non-constant key.  *  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|BindingBuilder
specifier|public
class|class
name|BindingBuilder
parameter_list|<
name|T
parameter_list|>
extends|extends
name|AbstractBindingBuilder
argument_list|<
name|T
argument_list|>
implements|implements
name|AnnotatedBindingBuilder
argument_list|<
name|T
argument_list|>
block|{
DECL|method|BindingBuilder
specifier|public
name|BindingBuilder
parameter_list|(
name|Binder
name|binder
parameter_list|,
name|List
argument_list|<
name|Element
argument_list|>
name|elements
parameter_list|,
name|Object
name|source
parameter_list|,
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|)
block|{
name|super
argument_list|(
name|binder
argument_list|,
name|elements
argument_list|,
name|source
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
DECL|method|annotatedWith
specifier|public
name|BindingBuilder
argument_list|<
name|T
argument_list|>
name|annotatedWith
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|annotationType
parameter_list|)
block|{
name|annotatedWithInternal
argument_list|(
name|annotationType
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|annotatedWith
specifier|public
name|BindingBuilder
argument_list|<
name|T
argument_list|>
name|annotatedWith
parameter_list|(
name|Annotation
name|annotation
parameter_list|)
block|{
name|annotatedWithInternal
argument_list|(
name|annotation
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|to
specifier|public
name|BindingBuilder
argument_list|<
name|T
argument_list|>
name|to
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|implementation
parameter_list|)
block|{
return|return
name|to
argument_list|(
name|Key
operator|.
name|get
argument_list|(
name|implementation
argument_list|)
argument_list|)
return|;
block|}
DECL|method|to
specifier|public
name|BindingBuilder
argument_list|<
name|T
argument_list|>
name|to
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|implementation
parameter_list|)
block|{
return|return
name|to
argument_list|(
name|Key
operator|.
name|get
argument_list|(
name|implementation
argument_list|)
argument_list|)
return|;
block|}
DECL|method|to
specifier|public
name|BindingBuilder
argument_list|<
name|T
argument_list|>
name|to
parameter_list|(
name|Key
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|linkedKey
parameter_list|)
block|{
name|checkNotNull
argument_list|(
name|linkedKey
argument_list|,
literal|"linkedKey"
argument_list|)
expr_stmt|;
name|checkNotTargetted
argument_list|()
expr_stmt|;
name|BindingImpl
argument_list|<
name|T
argument_list|>
name|base
init|=
name|getBinding
argument_list|()
decl_stmt|;
name|setBinding
argument_list|(
operator|new
name|LinkedBindingImpl
argument_list|<
name|T
argument_list|>
argument_list|(
name|base
operator|.
name|getSource
argument_list|()
argument_list|,
name|base
operator|.
name|getKey
argument_list|()
argument_list|,
name|base
operator|.
name|getScoping
argument_list|()
argument_list|,
name|linkedKey
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|toInstance
specifier|public
name|void
name|toInstance
parameter_list|(
name|T
name|instance
parameter_list|)
block|{
name|checkNotTargetted
argument_list|()
expr_stmt|;
comment|// lookup the injection points, adding any errors to the binder's errors list
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|injectionPoints
decl_stmt|;
if|if
condition|(
name|instance
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|injectionPoints
operator|=
name|InjectionPoint
operator|.
name|forInstanceMethodsAndFields
argument_list|(
name|instance
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ConfigurationException
name|e
parameter_list|)
block|{
for|for
control|(
name|Message
name|message
range|:
name|e
operator|.
name|getErrorMessages
argument_list|()
control|)
block|{
name|binder
operator|.
name|addError
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
name|injectionPoints
operator|=
name|e
operator|.
name|getPartialValue
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|binder
operator|.
name|addError
argument_list|(
name|BINDING_TO_NULL
argument_list|)
expr_stmt|;
name|injectionPoints
operator|=
name|ImmutableSet
operator|.
name|of
argument_list|()
expr_stmt|;
block|}
name|BindingImpl
argument_list|<
name|T
argument_list|>
name|base
init|=
name|getBinding
argument_list|()
decl_stmt|;
name|setBinding
argument_list|(
operator|new
name|InstanceBindingImpl
argument_list|<
name|T
argument_list|>
argument_list|(
name|base
operator|.
name|getSource
argument_list|()
argument_list|,
name|base
operator|.
name|getKey
argument_list|()
argument_list|,
name|base
operator|.
name|getScoping
argument_list|()
argument_list|,
name|injectionPoints
argument_list|,
name|instance
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|toProvider
specifier|public
name|BindingBuilder
argument_list|<
name|T
argument_list|>
name|toProvider
parameter_list|(
name|Provider
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|provider
parameter_list|)
block|{
name|checkNotNull
argument_list|(
name|provider
argument_list|,
literal|"provider"
argument_list|)
expr_stmt|;
name|checkNotTargetted
argument_list|()
expr_stmt|;
comment|// lookup the injection points, adding any errors to the binder's errors list
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|injectionPoints
decl_stmt|;
try|try
block|{
name|injectionPoints
operator|=
name|InjectionPoint
operator|.
name|forInstanceMethodsAndFields
argument_list|(
name|provider
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ConfigurationException
name|e
parameter_list|)
block|{
for|for
control|(
name|Message
name|message
range|:
name|e
operator|.
name|getErrorMessages
argument_list|()
control|)
block|{
name|binder
operator|.
name|addError
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
name|injectionPoints
operator|=
name|e
operator|.
name|getPartialValue
argument_list|()
expr_stmt|;
block|}
name|BindingImpl
argument_list|<
name|T
argument_list|>
name|base
init|=
name|getBinding
argument_list|()
decl_stmt|;
name|setBinding
argument_list|(
operator|new
name|ProviderInstanceBindingImpl
argument_list|<
name|T
argument_list|>
argument_list|(
name|base
operator|.
name|getSource
argument_list|()
argument_list|,
name|base
operator|.
name|getKey
argument_list|()
argument_list|,
name|base
operator|.
name|getScoping
argument_list|()
argument_list|,
name|injectionPoints
argument_list|,
name|provider
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|toProvider
specifier|public
name|BindingBuilder
argument_list|<
name|T
argument_list|>
name|toProvider
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Provider
argument_list|<
name|?
extends|extends
name|T
argument_list|>
argument_list|>
name|providerType
parameter_list|)
block|{
return|return
name|toProvider
argument_list|(
name|Key
operator|.
name|get
argument_list|(
name|providerType
argument_list|)
argument_list|)
return|;
block|}
DECL|method|toProvider
specifier|public
name|BindingBuilder
argument_list|<
name|T
argument_list|>
name|toProvider
parameter_list|(
name|Key
argument_list|<
name|?
extends|extends
name|Provider
argument_list|<
name|?
extends|extends
name|T
argument_list|>
argument_list|>
name|providerKey
parameter_list|)
block|{
name|checkNotNull
argument_list|(
name|providerKey
argument_list|,
literal|"providerKey"
argument_list|)
expr_stmt|;
name|checkNotTargetted
argument_list|()
expr_stmt|;
name|BindingImpl
argument_list|<
name|T
argument_list|>
name|base
init|=
name|getBinding
argument_list|()
decl_stmt|;
name|setBinding
argument_list|(
operator|new
name|LinkedProviderBindingImpl
argument_list|<
name|T
argument_list|>
argument_list|(
name|base
operator|.
name|getSource
argument_list|()
argument_list|,
name|base
operator|.
name|getKey
argument_list|()
argument_list|,
name|base
operator|.
name|getScoping
argument_list|()
argument_list|,
name|providerKey
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"BindingBuilder<"
operator|+
name|getBinding
argument_list|()
operator|.
name|getKey
argument_list|()
operator|.
name|getTypeLiteral
argument_list|()
operator|+
literal|">"
return|;
block|}
block|}
end_class

end_unit

