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
name|TypeLiteral
import|;
end_import

begin_comment
comment|/**  * Context of an injectable type encounter. Enables reporting errors, registering injection  * listeners and binding method interceptors for injectable type {@code I}. It is an error to use  * an encounter after the {@link TypeListener#hear(TypeLiteral, TypeEncounter) hear()} method has  * returned.  *  * @param<I> the injectable type encountered  * @since 2.0  */
end_comment

begin_interface
annotation|@
name|SuppressWarnings
argument_list|(
literal|"overloads"
argument_list|)
DECL|interface|TypeEncounter
specifier|public
interface|interface
name|TypeEncounter
parameter_list|<
name|I
parameter_list|>
block|{
comment|/**      * Records an error message for type {@code I} which will be presented to the user at a later      * time. Unlike throwing an exception, this enable us to continue configuring the Injector and      * discover more errors. Uses {@link String#format(String, Object[])} to insert the arguments      * into the message.      */
DECL|method|addError
name|void
name|addError
parameter_list|(
name|String
name|message
parameter_list|,
name|Object
modifier|...
name|arguments
parameter_list|)
function_decl|;
comment|/**      * Records an exception for type {@code I}, the full details of which will be logged, and the      * message of which will be presented to the user at a later time. If your type listener calls      * something that you worry may fail, you should catch the exception and pass it to this method.      */
DECL|method|addError
name|void
name|addError
parameter_list|(
name|Throwable
name|t
parameter_list|)
function_decl|;
comment|/**      * Records an error message to be presented to the user at a later time.      */
DECL|method|addError
name|void
name|addError
parameter_list|(
name|Message
name|message
parameter_list|)
function_decl|;
comment|/**      * Returns the provider used to obtain instances for the given injection key. The returned      * provider will not be valid until the injector has been created. The provider will throw an      * {@code IllegalStateException} if you try to use it beforehand.      */
DECL|method|getProvider
parameter_list|<
name|T
parameter_list|>
name|Provider
argument_list|<
name|T
argument_list|>
name|getProvider
parameter_list|(
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|)
function_decl|;
comment|/**      * Returns the provider used to obtain instances for the given injection type. The returned      * provider will not be valid until the injetor has been created. The provider will throw an      * {@code IllegalStateException} if you try to use it beforehand.      */
DECL|method|getProvider
parameter_list|<
name|T
parameter_list|>
name|Provider
argument_list|<
name|T
argument_list|>
name|getProvider
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|type
parameter_list|)
function_decl|;
comment|/**      * Returns the members injector used to inject dependencies into methods and fields on instances      * of the given type {@code T}. The returned members injector will not be valid until the main      * injector has been created. The members injector will throw an {@code IllegalStateException}      * if you try to use it beforehand.      *      * @param typeLiteral type to get members injector for      */
DECL|method|getMembersInjector
parameter_list|<
name|T
parameter_list|>
name|MembersInjector
argument_list|<
name|T
argument_list|>
name|getMembersInjector
parameter_list|(
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|typeLiteral
parameter_list|)
function_decl|;
comment|/**      * Returns the members injector used to inject dependencies into methods and fields on instances      * of the given type {@code T}. The returned members injector will not be valid until the main      * injector has been created. The members injector will throw an {@code IllegalStateException}      * if you try to use it beforehand.      *      * @param type type to get members injector for      */
DECL|method|getMembersInjector
parameter_list|<
name|T
parameter_list|>
name|MembersInjector
argument_list|<
name|T
argument_list|>
name|getMembersInjector
parameter_list|(
name|Class
argument_list|<
name|T
argument_list|>
name|type
parameter_list|)
function_decl|;
comment|/**      * Registers a members injector for type {@code I}. Guice will use the members injector after its      * performed its own injections on an instance of {@code I}.      */
DECL|method|register
name|void
name|register
parameter_list|(
name|MembersInjector
argument_list|<
name|?
super|super
name|I
argument_list|>
name|membersInjector
parameter_list|)
function_decl|;
comment|/**      * Registers an injection listener for type {@code I}. Guice will notify the listener after all      * injections have been performed on an instance of {@code I}.      */
DECL|method|register
name|void
name|register
parameter_list|(
name|InjectionListener
argument_list|<
name|?
super|super
name|I
argument_list|>
name|listener
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

