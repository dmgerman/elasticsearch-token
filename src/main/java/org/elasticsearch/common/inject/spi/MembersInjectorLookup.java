begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2009 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|MembersInjector
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
name|TypeLiteral
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
operator|.
name|checkNotNull
import|;
end_import

begin_import
import|import static
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
operator|.
name|checkState
import|;
end_import

begin_comment
comment|/**  * A lookup of the members injector for a type. Lookups are created explicitly in a module using  * {@link org.elasticsearch.common.inject.Binder#getMembersInjector(Class) getMembersInjector()} statements:  *<pre>  *     MembersInjector&lt;PaymentService&gt; membersInjector  *         = getMembersInjector(PaymentService.class);</pre>  *  * @author crazybob@google.com (Bob Lee)  * @since 2.0  */
end_comment

begin_class
DECL|class|MembersInjectorLookup
specifier|public
specifier|final
class|class
name|MembersInjectorLookup
parameter_list|<
name|T
parameter_list|>
implements|implements
name|Element
block|{
DECL|field|source
specifier|private
specifier|final
name|Object
name|source
decl_stmt|;
DECL|field|type
specifier|private
specifier|final
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|type
decl_stmt|;
DECL|field|delegate
specifier|private
name|MembersInjector
argument_list|<
name|T
argument_list|>
name|delegate
decl_stmt|;
DECL|method|MembersInjectorLookup
specifier|public
name|MembersInjectorLookup
parameter_list|(
name|Object
name|source
parameter_list|,
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|type
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|checkNotNull
argument_list|(
name|source
argument_list|,
literal|"source"
argument_list|)
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|checkNotNull
argument_list|(
name|type
argument_list|,
literal|"type"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
comment|/**      * Gets the type containing the members to be injected.      */
DECL|method|getType
specifier|public
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|getType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
annotation|@
name|Override
DECL|method|acceptVisitor
specifier|public
parameter_list|<
name|T
parameter_list|>
name|T
name|acceptVisitor
parameter_list|(
name|ElementVisitor
argument_list|<
name|T
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
comment|/**      * Sets the actual members injector.      *      * @throws IllegalStateException if the delegate is already set      */
DECL|method|initializeDelegate
specifier|public
name|void
name|initializeDelegate
parameter_list|(
name|MembersInjector
argument_list|<
name|T
argument_list|>
name|delegate
parameter_list|)
block|{
name|checkState
argument_list|(
name|this
operator|.
name|delegate
operator|==
literal|null
argument_list|,
literal|"delegate already initialized"
argument_list|)
expr_stmt|;
name|this
operator|.
name|delegate
operator|=
name|checkNotNull
argument_list|(
name|delegate
argument_list|,
literal|"delegate"
argument_list|)
expr_stmt|;
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
name|initializeDelegate
argument_list|(
name|binder
operator|.
name|withSource
argument_list|(
name|getSource
argument_list|()
argument_list|)
operator|.
name|getMembersInjector
argument_list|(
name|type
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the delegate members injector, or {@code null} if it has not yet been initialized.      * The delegate will be initialized when this element is processed, or otherwise used to create      * an injector.      */
DECL|method|getDelegate
specifier|public
name|MembersInjector
argument_list|<
name|T
argument_list|>
name|getDelegate
parameter_list|()
block|{
return|return
name|delegate
return|;
block|}
comment|/**      * Returns the looked up members injector. The result is not valid until this lookup has been      * initialized, which usually happens when the injector is created. The members injector will      * throw an {@code IllegalStateException} if you try to use it beforehand.      */
DECL|method|getMembersInjector
specifier|public
name|MembersInjector
argument_list|<
name|T
argument_list|>
name|getMembersInjector
parameter_list|()
block|{
return|return
operator|new
name|MembersInjector
argument_list|<
name|T
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|injectMembers
parameter_list|(
name|T
name|instance
parameter_list|)
block|{
name|checkState
argument_list|(
name|delegate
operator|!=
literal|null
argument_list|,
literal|"This MembersInjector cannot be used until the Injector has been created."
argument_list|)
expr_stmt|;
name|delegate
operator|.
name|injectMembers
argument_list|(
name|instance
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"MembersInjector<"
operator|+
name|type
operator|+
literal|">"
return|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit

