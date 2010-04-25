begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.guice.inject
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
name|internal
operator|.
name|Errors
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
name|ElementVisitor
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
name|InjectionRequest
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
name|MembersInjectorLookup
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
name|guice
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
name|ProviderLookup
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
name|ScopeBinding
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
name|StaticInjectionRequest
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
name|TypeConverterBinding
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
name|TypeListenerBinding
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
comment|/**  * Abstract base class for creating an injector from module elements.  *  *<p>Extending classes must return {@code true} from any overridden  * {@code visit*()} methods, in order for the element processor to remove the  * handled element.  *  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|AbstractProcessor
specifier|abstract
class|class
name|AbstractProcessor
implements|implements
name|ElementVisitor
argument_list|<
name|Boolean
argument_list|>
block|{
DECL|field|errors
specifier|protected
name|Errors
name|errors
decl_stmt|;
DECL|field|injector
specifier|protected
name|InjectorImpl
name|injector
decl_stmt|;
DECL|method|AbstractProcessor
specifier|protected
name|AbstractProcessor
parameter_list|(
name|Errors
name|errors
parameter_list|)
block|{
name|this
operator|.
name|errors
operator|=
name|errors
expr_stmt|;
block|}
DECL|method|process
specifier|public
name|void
name|process
parameter_list|(
name|Iterable
argument_list|<
name|InjectorShell
argument_list|>
name|isolatedInjectorBuilders
parameter_list|)
block|{
for|for
control|(
name|InjectorShell
name|injectorShell
range|:
name|isolatedInjectorBuilders
control|)
block|{
name|process
argument_list|(
name|injectorShell
operator|.
name|getInjector
argument_list|()
argument_list|,
name|injectorShell
operator|.
name|getElements
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|process
specifier|public
name|void
name|process
parameter_list|(
name|InjectorImpl
name|injector
parameter_list|,
name|List
argument_list|<
name|Element
argument_list|>
name|elements
parameter_list|)
block|{
name|Errors
name|errorsAnyElement
init|=
name|this
operator|.
name|errors
decl_stmt|;
name|this
operator|.
name|injector
operator|=
name|injector
expr_stmt|;
try|try
block|{
for|for
control|(
name|Iterator
argument_list|<
name|Element
argument_list|>
name|i
init|=
name|elements
operator|.
name|iterator
argument_list|()
init|;
name|i
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|Element
name|element
init|=
name|i
operator|.
name|next
argument_list|()
decl_stmt|;
name|this
operator|.
name|errors
operator|=
name|errorsAnyElement
operator|.
name|withSource
argument_list|(
name|element
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
name|Boolean
name|allDone
init|=
name|element
operator|.
name|acceptVisitor
argument_list|(
name|this
argument_list|)
decl_stmt|;
if|if
condition|(
name|allDone
condition|)
block|{
name|i
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|this
operator|.
name|errors
operator|=
name|errorsAnyElement
expr_stmt|;
name|this
operator|.
name|injector
operator|=
literal|null
expr_stmt|;
block|}
block|}
DECL|method|visit
specifier|public
name|Boolean
name|visit
parameter_list|(
name|Message
name|message
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|visit
specifier|public
name|Boolean
name|visit
parameter_list|(
name|ScopeBinding
name|scopeBinding
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|visit
specifier|public
name|Boolean
name|visit
parameter_list|(
name|InjectionRequest
name|injectionRequest
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|visit
specifier|public
name|Boolean
name|visit
parameter_list|(
name|StaticInjectionRequest
name|staticInjectionRequest
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|visit
specifier|public
name|Boolean
name|visit
parameter_list|(
name|TypeConverterBinding
name|typeConverterBinding
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|visit
specifier|public
parameter_list|<
name|T
parameter_list|>
name|Boolean
name|visit
parameter_list|(
name|Binding
argument_list|<
name|T
argument_list|>
name|binding
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|visit
specifier|public
parameter_list|<
name|T
parameter_list|>
name|Boolean
name|visit
parameter_list|(
name|ProviderLookup
argument_list|<
name|T
argument_list|>
name|providerLookup
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|visit
specifier|public
name|Boolean
name|visit
parameter_list|(
name|PrivateElements
name|privateElements
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|visit
specifier|public
parameter_list|<
name|T
parameter_list|>
name|Boolean
name|visit
parameter_list|(
name|MembersInjectorLookup
argument_list|<
name|T
argument_list|>
name|lookup
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
DECL|method|visit
specifier|public
name|Boolean
name|visit
parameter_list|(
name|TypeListenerBinding
name|binding
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

