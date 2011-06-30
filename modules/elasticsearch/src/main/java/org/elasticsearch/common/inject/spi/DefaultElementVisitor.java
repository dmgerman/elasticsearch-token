begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
comment|/**  * No-op visitor for subclassing. All interface methods simply delegate to  * {@link #visitOther(Element)}, returning its result.  *  * @param<V> any type to be returned by the visit method. Use {@link Void} with  *            {@code return null} if no return type is needed.  * @author sberlin@gmail.com (Sam Berlin)  * @since 2.0  */
end_comment

begin_class
DECL|class|DefaultElementVisitor
specifier|public
specifier|abstract
class|class
name|DefaultElementVisitor
parameter_list|<
name|V
parameter_list|>
implements|implements
name|ElementVisitor
argument_list|<
name|V
argument_list|>
block|{
comment|/**      * Default visit implementation. Returns {@code null}.      */
DECL|method|visitOther
specifier|protected
name|V
name|visitOther
parameter_list|(
name|Element
name|element
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|Message
name|message
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|message
argument_list|)
return|;
block|}
DECL|method|visit
specifier|public
parameter_list|<
name|T
parameter_list|>
name|V
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
name|visitOther
argument_list|(
name|binding
argument_list|)
return|;
block|}
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|ScopeBinding
name|scopeBinding
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|scopeBinding
argument_list|)
return|;
block|}
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|TypeConverterBinding
name|typeConverterBinding
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|typeConverterBinding
argument_list|)
return|;
block|}
DECL|method|visit
specifier|public
parameter_list|<
name|T
parameter_list|>
name|V
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
name|visitOther
argument_list|(
name|providerLookup
argument_list|)
return|;
block|}
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|InjectionRequest
name|injectionRequest
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|injectionRequest
argument_list|)
return|;
block|}
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|StaticInjectionRequest
name|staticInjectionRequest
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|staticInjectionRequest
argument_list|)
return|;
block|}
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|PrivateElements
name|privateElements
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|privateElements
argument_list|)
return|;
block|}
DECL|method|visit
specifier|public
parameter_list|<
name|T
parameter_list|>
name|V
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
name|visitOther
argument_list|(
name|lookup
argument_list|)
return|;
block|}
DECL|method|visit
specifier|public
name|V
name|visit
parameter_list|(
name|TypeListenerBinding
name|binding
parameter_list|)
block|{
return|return
name|visitOther
argument_list|(
name|binding
argument_list|)
return|;
block|}
block|}
end_class

end_unit

