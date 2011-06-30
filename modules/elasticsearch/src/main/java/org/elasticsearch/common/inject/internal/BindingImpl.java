begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2006 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Binding
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
name|BindingScopingVisitor
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
name|ElementVisitor
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
name|InstanceBinding
import|;
end_import

begin_comment
comment|/**  * @author crazybob@google.com (Bob Lee)  */
end_comment

begin_class
DECL|class|BindingImpl
specifier|public
specifier|abstract
class|class
name|BindingImpl
parameter_list|<
name|T
parameter_list|>
implements|implements
name|Binding
argument_list|<
name|T
argument_list|>
block|{
DECL|field|injector
specifier|private
specifier|final
name|Injector
name|injector
decl_stmt|;
DECL|field|key
specifier|private
specifier|final
name|Key
argument_list|<
name|T
argument_list|>
name|key
decl_stmt|;
DECL|field|source
specifier|private
specifier|final
name|Object
name|source
decl_stmt|;
DECL|field|scoping
specifier|private
specifier|final
name|Scoping
name|scoping
decl_stmt|;
DECL|field|internalFactory
specifier|private
specifier|final
name|InternalFactory
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|internalFactory
decl_stmt|;
DECL|method|BindingImpl
specifier|public
name|BindingImpl
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
name|Scoping
name|scoping
parameter_list|)
block|{
name|this
operator|.
name|injector
operator|=
name|injector
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|internalFactory
operator|=
name|internalFactory
expr_stmt|;
name|this
operator|.
name|scoping
operator|=
name|scoping
expr_stmt|;
block|}
DECL|method|BindingImpl
specifier|protected
name|BindingImpl
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
name|this
operator|.
name|internalFactory
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|injector
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|scoping
operator|=
name|scoping
expr_stmt|;
block|}
DECL|method|getKey
specifier|public
name|Key
argument_list|<
name|T
argument_list|>
name|getKey
parameter_list|()
block|{
return|return
name|key
return|;
block|}
DECL|method|getSource
specifier|public
name|Object
name|getSource
parameter_list|()
block|{
return|return
name|source
return|;
block|}
DECL|field|provider
specifier|private
specifier|volatile
name|Provider
argument_list|<
name|T
argument_list|>
name|provider
decl_stmt|;
DECL|method|getProvider
specifier|public
name|Provider
argument_list|<
name|T
argument_list|>
name|getProvider
parameter_list|()
block|{
if|if
condition|(
name|provider
operator|==
literal|null
condition|)
block|{
if|if
condition|(
name|injector
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"getProvider() not supported for module bindings"
argument_list|)
throw|;
block|}
name|provider
operator|=
name|injector
operator|.
name|getProvider
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
return|return
name|provider
return|;
block|}
DECL|method|getInternalFactory
specifier|public
name|InternalFactory
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|getInternalFactory
parameter_list|()
block|{
return|return
name|internalFactory
return|;
block|}
DECL|method|getScoping
specifier|public
name|Scoping
name|getScoping
parameter_list|()
block|{
return|return
name|scoping
return|;
block|}
comment|/**      * Is this a constant binding? This returns true for constant bindings as      * well as toInstance() bindings.      */
DECL|method|isConstant
specifier|public
name|boolean
name|isConstant
parameter_list|()
block|{
return|return
name|this
operator|instanceof
name|InstanceBinding
return|;
block|}
DECL|method|acceptVisitor
specifier|public
parameter_list|<
name|V
parameter_list|>
name|V
name|acceptVisitor
parameter_list|(
name|ElementVisitor
argument_list|<
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
DECL|method|acceptScopingVisitor
specifier|public
parameter_list|<
name|V
parameter_list|>
name|V
name|acceptScopingVisitor
parameter_list|(
name|BindingScopingVisitor
argument_list|<
name|V
argument_list|>
name|visitor
parameter_list|)
block|{
return|return
name|scoping
operator|.
name|acceptVisitor
argument_list|(
name|visitor
argument_list|)
return|;
block|}
DECL|method|withScoping
specifier|protected
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
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
DECL|method|withKey
specifier|protected
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
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
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
name|Binding
operator|.
name|class
argument_list|)
operator|.
name|add
argument_list|(
literal|"key"
argument_list|,
name|key
argument_list|)
operator|.
name|add
argument_list|(
literal|"scope"
argument_list|,
name|scoping
argument_list|)
operator|.
name|add
argument_list|(
literal|"source"
argument_list|,
name|source
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|getInjector
specifier|public
name|Injector
name|getInjector
parameter_list|()
block|{
return|return
name|injector
return|;
block|}
block|}
end_class

end_unit

