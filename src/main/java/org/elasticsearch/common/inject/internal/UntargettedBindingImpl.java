begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2007 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|UntargettedBinding
import|;
end_import

begin_class
DECL|class|UntargettedBindingImpl
specifier|public
class|class
name|UntargettedBindingImpl
parameter_list|<
name|T
parameter_list|>
extends|extends
name|BindingImpl
argument_list|<
name|T
argument_list|>
implements|implements
name|UntargettedBinding
argument_list|<
name|T
argument_list|>
block|{
DECL|method|UntargettedBindingImpl
specifier|public
name|UntargettedBindingImpl
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
operator|new
name|InternalFactory
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|T
name|get
parameter_list|(
name|Errors
name|errors
parameter_list|,
name|InternalContext
name|context
parameter_list|,
name|Dependency
argument_list|<
name|?
argument_list|>
name|dependency
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
argument_list|,
name|Scoping
operator|.
name|UNSCOPED
argument_list|)
expr_stmt|;
block|}
DECL|method|UntargettedBindingImpl
specifier|public
name|UntargettedBindingImpl
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
name|UntargettedBindingImpl
argument_list|<>
argument_list|(
name|getSource
argument_list|()
argument_list|,
name|getKey
argument_list|()
argument_list|,
name|scoping
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|UntargettedBindingImpl
argument_list|<>
argument_list|(
name|getSource
argument_list|()
argument_list|,
name|key
argument_list|,
name|getScoping
argument_list|()
argument_list|)
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
name|getScoping
argument_list|()
operator|.
name|applyTo
argument_list|(
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
argument_list|)
expr_stmt|;
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
name|UntargettedBinding
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
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

