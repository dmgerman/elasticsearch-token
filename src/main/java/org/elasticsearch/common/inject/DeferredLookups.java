begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2009 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|common
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
name|common
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
name|common
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
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * Returns providers and members injectors that haven't yet been initialized. As a part of injector  * creation it's necessary to {@link #initialize initialize} these lookups.  *  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|DeferredLookups
class|class
name|DeferredLookups
implements|implements
name|Lookups
block|{
DECL|field|injector
specifier|private
specifier|final
name|InjectorImpl
name|injector
decl_stmt|;
DECL|field|lookups
specifier|private
specifier|final
name|List
argument_list|<
name|Element
argument_list|>
name|lookups
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
DECL|method|DeferredLookups
specifier|public
name|DeferredLookups
parameter_list|(
name|InjectorImpl
name|injector
parameter_list|)
block|{
name|this
operator|.
name|injector
operator|=
name|injector
expr_stmt|;
block|}
comment|/**      * Initialize the specified lookups, either immediately or when the injector is created.      */
DECL|method|initialize
specifier|public
name|void
name|initialize
parameter_list|(
name|Errors
name|errors
parameter_list|)
block|{
name|injector
operator|.
name|lookups
operator|=
name|injector
expr_stmt|;
operator|new
name|LookupProcessor
argument_list|(
name|errors
argument_list|)
operator|.
name|process
argument_list|(
name|injector
argument_list|,
name|lookups
argument_list|)
expr_stmt|;
block|}
DECL|method|getProvider
specifier|public
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
block|{
name|ProviderLookup
argument_list|<
name|T
argument_list|>
name|lookup
init|=
operator|new
name|ProviderLookup
argument_list|<>
argument_list|(
name|key
argument_list|,
name|key
argument_list|)
decl_stmt|;
name|lookups
operator|.
name|add
argument_list|(
name|lookup
argument_list|)
expr_stmt|;
return|return
name|lookup
operator|.
name|getProvider
argument_list|()
return|;
block|}
DECL|method|getMembersInjector
specifier|public
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
name|type
parameter_list|)
block|{
name|MembersInjectorLookup
argument_list|<
name|T
argument_list|>
name|lookup
init|=
operator|new
name|MembersInjectorLookup
argument_list|<>
argument_list|(
name|type
argument_list|,
name|type
argument_list|)
decl_stmt|;
name|lookups
operator|.
name|add
argument_list|(
name|lookup
argument_list|)
expr_stmt|;
return|return
name|lookup
operator|.
name|getMembersInjector
argument_list|()
return|;
block|}
block|}
end_class

end_unit

