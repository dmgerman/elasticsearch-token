begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
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
name|common
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
name|common
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
name|common
operator|.
name|inject
operator|.
name|spi
operator|.
name|ExposedBinding
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
name|spi
operator|.
name|PrivateElements
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
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singleton
import|;
end_import

begin_class
DECL|class|ExposedBindingImpl
specifier|public
class|class
name|ExposedBindingImpl
parameter_list|<
name|T
parameter_list|>
extends|extends
name|BindingImpl
argument_list|<
name|T
argument_list|>
implements|implements
name|ExposedBinding
argument_list|<
name|T
argument_list|>
block|{
DECL|field|privateElements
specifier|private
specifier|final
name|PrivateElements
name|privateElements
decl_stmt|;
DECL|method|ExposedBindingImpl
specifier|public
name|ExposedBindingImpl
parameter_list|(
name|Injector
name|injector
parameter_list|,
name|Object
name|source
parameter_list|,
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|,
name|InternalFactory
argument_list|<
name|T
argument_list|>
name|factory
parameter_list|,
name|PrivateElements
name|privateElements
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
name|factory
argument_list|,
name|Scoping
operator|.
name|UNSCOPED
argument_list|)
expr_stmt|;
name|this
operator|.
name|privateElements
operator|=
name|privateElements
expr_stmt|;
block|}
DECL|method|ExposedBindingImpl
specifier|public
name|ExposedBindingImpl
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
name|PrivateElements
name|privateElements
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
name|privateElements
operator|=
name|privateElements
expr_stmt|;
block|}
annotation|@
name|Override
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
annotation|@
name|Override
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
name|singleton
argument_list|(
name|Dependency
operator|.
name|get
argument_list|(
name|Key
operator|.
name|get
argument_list|(
name|Injector
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getPrivateElements
specifier|public
name|PrivateElements
name|getPrivateElements
parameter_list|()
block|{
return|return
name|privateElements
return|;
block|}
annotation|@
name|Override
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
name|ExposedBindingImpl
argument_list|<>
argument_list|(
name|getSource
argument_list|()
argument_list|,
name|getKey
argument_list|()
argument_list|,
name|scoping
argument_list|,
name|privateElements
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|withKey
specifier|public
name|ExposedBindingImpl
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
name|ExposedBindingImpl
argument_list|<>
argument_list|(
name|getSource
argument_list|()
argument_list|,
name|key
argument_list|,
name|getScoping
argument_list|()
argument_list|,
name|privateElements
argument_list|)
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
operator|new
name|ToStringBuilder
argument_list|(
name|ExposedBinding
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
literal|"privateElements"
argument_list|,
name|privateElements
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|applyTo
specifier|public
name|void
name|applyTo
parameter_list|(
name|Binder
name|binder
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"This element represents a synthetic binding."
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

