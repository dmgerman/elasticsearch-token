begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2006 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.guice.inject.binder
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
name|binder
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

begin_comment
comment|/**  * See the EDSL examples at {@link org.elasticsearch.util.guice.inject.Binder}.  *  * @author crazybob@google.com (Bob Lee)  */
end_comment

begin_interface
DECL|interface|LinkedBindingBuilder
specifier|public
interface|interface
name|LinkedBindingBuilder
parameter_list|<
name|T
parameter_list|>
extends|extends
name|ScopedBindingBuilder
block|{
comment|/**    * See the EDSL examples at {@link org.elasticsearch.util.guice.inject.Binder}.    */
DECL|method|to
name|ScopedBindingBuilder
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
function_decl|;
comment|/**    * See the EDSL examples at {@link org.elasticsearch.util.guice.inject.Binder}.    */
DECL|method|to
name|ScopedBindingBuilder
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
function_decl|;
comment|/**    * See the EDSL examples at {@link org.elasticsearch.util.guice.inject.Binder}.    */
DECL|method|to
name|ScopedBindingBuilder
name|to
parameter_list|(
name|Key
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|targetKey
parameter_list|)
function_decl|;
comment|/**    * See the EDSL examples at {@link org.elasticsearch.util.guice.inject.Binder}.    *    * @see org.elasticsearch.util.guice.inject.Injector#injectMembers    */
DECL|method|toInstance
name|void
name|toInstance
parameter_list|(
name|T
name|instance
parameter_list|)
function_decl|;
comment|/**    * See the EDSL examples at {@link org.elasticsearch.util.guice.inject.Binder}.    *    * @see org.elasticsearch.util.guice.inject.Injector#injectMembers    */
DECL|method|toProvider
name|ScopedBindingBuilder
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
function_decl|;
comment|/**    * See the EDSL examples at {@link org.elasticsearch.util.guice.inject.Binder}.    */
DECL|method|toProvider
name|ScopedBindingBuilder
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
function_decl|;
comment|/**    * See the EDSL examples at {@link org.elasticsearch.util.guice.inject.Binder}.    */
DECL|method|toProvider
name|ScopedBindingBuilder
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
function_decl|;
block|}
end_interface

end_unit

