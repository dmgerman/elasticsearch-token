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
name|Injector
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
name|spi
operator|.
name|BindingTargetVisitor
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
name|Dependency
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
name|HasDependencies
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
name|InstanceBinding
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
name|util
operator|.
name|Providers
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
name|util
operator|.
name|Set
import|;
end_import

begin_class
DECL|class|InstanceBindingImpl
specifier|public
class|class
name|InstanceBindingImpl
parameter_list|<
name|T
parameter_list|>
extends|extends
name|BindingImpl
argument_list|<
name|T
argument_list|>
implements|implements
name|InstanceBinding
argument_list|<
name|T
argument_list|>
block|{
DECL|field|instance
specifier|final
name|T
name|instance
decl_stmt|;
DECL|field|provider
specifier|final
name|Provider
argument_list|<
name|T
argument_list|>
name|provider
decl_stmt|;
DECL|field|injectionPoints
specifier|final
name|ImmutableSet
argument_list|<
name|InjectionPoint
argument_list|>
name|injectionPoints
decl_stmt|;
DECL|method|InstanceBindingImpl
specifier|public
name|InstanceBindingImpl
parameter_list|(
name|Injector
name|injector
parameter_list|,
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|,
name|Object
name|source
parameter_list|,
name|InternalFactory
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|internalFactory
parameter_list|,
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|injectionPoints
parameter_list|,
name|T
name|instance
parameter_list|)
block|{
name|super
argument_list|(
name|injector
argument_list|,
name|key
argument_list|,
name|source
argument_list|,
name|internalFactory
argument_list|,
name|Scoping
operator|.
name|UNSCOPED
argument_list|)
expr_stmt|;
name|this
operator|.
name|injectionPoints
operator|=
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|injectionPoints
argument_list|)
expr_stmt|;
name|this
operator|.
name|instance
operator|=
name|instance
expr_stmt|;
name|this
operator|.
name|provider
operator|=
name|Providers
operator|.
name|of
argument_list|(
name|instance
argument_list|)
expr_stmt|;
block|}
DECL|method|InstanceBindingImpl
specifier|public
name|InstanceBindingImpl
parameter_list|(
name|Object
name|source
parameter_list|,
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|,
name|Scoping
name|scoping
parameter_list|,
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|injectionPoints
parameter_list|,
name|T
name|instance
parameter_list|)
block|{
name|super
argument_list|(
name|source
argument_list|,
name|key
argument_list|,
name|scoping
argument_list|)
expr_stmt|;
name|this
operator|.
name|injectionPoints
operator|=
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|injectionPoints
argument_list|)
expr_stmt|;
name|this
operator|.
name|instance
operator|=
name|instance
expr_stmt|;
name|this
operator|.
name|provider
operator|=
name|Providers
operator|.
name|of
argument_list|(
name|instance
argument_list|)
expr_stmt|;
block|}
DECL|method|getProvider
annotation|@
name|Override
specifier|public
name|Provider
argument_list|<
name|T
argument_list|>
name|getProvider
parameter_list|()
block|{
return|return
name|this
operator|.
name|provider
return|;
block|}
DECL|method|acceptTargetVisitor
specifier|public
parameter_list|<
name|V
parameter_list|>
name|V
name|acceptTargetVisitor
parameter_list|(
name|BindingTargetVisitor
argument_list|<
name|?
super|super
name|T
argument_list|,
name|V
argument_list|>
name|visitor
parameter_list|)
block|{
return|return
name|visitor
operator|.
name|visit
argument_list|(
name|this
argument_list|)
return|;
block|}
DECL|method|getInstance
specifier|public
name|T
name|getInstance
parameter_list|()
block|{
return|return
name|instance
return|;
block|}
DECL|method|getInjectionPoints
specifier|public
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|getInjectionPoints
parameter_list|()
block|{
return|return
name|injectionPoints
return|;
block|}
DECL|method|getDependencies
specifier|public
name|Set
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|getDependencies
parameter_list|()
block|{
return|return
name|instance
operator|instanceof
name|HasDependencies
condition|?
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
operator|(
operator|(
name|HasDependencies
operator|)
name|instance
operator|)
operator|.
name|getDependencies
argument_list|()
argument_list|)
else|:
name|Dependency
operator|.
name|forInjectionPoints
argument_list|(
name|injectionPoints
argument_list|)
return|;
block|}
DECL|method|withScoping
specifier|public
name|BindingImpl
argument_list|<
name|T
argument_list|>
name|withScoping
parameter_list|(
name|Scoping
name|scoping
parameter_list|)
block|{
return|return
operator|new
name|InstanceBindingImpl
argument_list|<
name|T
argument_list|>
argument_list|(
name|getSource
argument_list|()
argument_list|,
name|getKey
argument_list|()
argument_list|,
name|scoping
argument_list|,
name|injectionPoints
argument_list|,
name|instance
argument_list|)
return|;
block|}
DECL|method|withKey
specifier|public
name|BindingImpl
argument_list|<
name|T
argument_list|>
name|withKey
parameter_list|(
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|)
block|{
return|return
operator|new
name|InstanceBindingImpl
argument_list|<
name|T
argument_list|>
argument_list|(
name|getSource
argument_list|()
argument_list|,
name|key
argument_list|,
name|getScoping
argument_list|()
argument_list|,
name|injectionPoints
argument_list|,
name|instance
argument_list|)
return|;
block|}
DECL|method|applyTo
specifier|public
name|void
name|applyTo
parameter_list|(
name|Binder
name|binder
parameter_list|)
block|{
comment|// instance bindings aren't scoped
name|binder
operator|.
name|withSource
argument_list|(
name|getSource
argument_list|()
argument_list|)
operator|.
name|bind
argument_list|(
name|getKey
argument_list|()
argument_list|)
operator|.
name|toInstance
argument_list|(
name|instance
argument_list|)
expr_stmt|;
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
operator|new
name|ToStringBuilder
argument_list|(
name|InstanceBinding
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
literal|"key"
argument_list|,
name|getKey
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
literal|"source"
argument_list|,
name|getSource
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
literal|"instance"
argument_list|,
name|instance
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

