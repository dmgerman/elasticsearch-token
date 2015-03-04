begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|*
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
name|PrivateElements
import|;
end_import

begin_comment
comment|/**  * This factory exists in a parent injector. When invoked, it retrieves its value from a child  * injector.  */
end_comment

begin_class
DECL|class|ExposedKeyFactory
class|class
name|ExposedKeyFactory
parameter_list|<
name|T
parameter_list|>
implements|implements
name|InternalFactory
argument_list|<
name|T
argument_list|>
implements|,
name|BindingProcessor
operator|.
name|CreationListener
block|{
DECL|field|key
specifier|private
specifier|final
name|Key
argument_list|<
name|T
argument_list|>
name|key
decl_stmt|;
DECL|field|privateElements
specifier|private
specifier|final
name|PrivateElements
name|privateElements
decl_stmt|;
DECL|field|delegate
specifier|private
name|BindingImpl
argument_list|<
name|T
argument_list|>
name|delegate
decl_stmt|;
DECL|method|ExposedKeyFactory
specifier|public
name|ExposedKeyFactory
parameter_list|(
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|,
name|PrivateElements
name|privateElements
parameter_list|)
block|{
name|this
operator|.
name|key
operator|=
name|key
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
DECL|method|notify
specifier|public
name|void
name|notify
parameter_list|(
name|Errors
name|errors
parameter_list|)
block|{
name|InjectorImpl
name|privateInjector
init|=
operator|(
name|InjectorImpl
operator|)
name|privateElements
operator|.
name|getInjector
argument_list|()
decl_stmt|;
name|BindingImpl
argument_list|<
name|T
argument_list|>
name|explicitBinding
init|=
name|privateInjector
operator|.
name|state
operator|.
name|getExplicitBinding
argument_list|(
name|key
argument_list|)
decl_stmt|;
comment|// validate that the child injector has its own factory. If the getInternalFactory() returns
comment|// this, then that child injector doesn't have a factory (and getExplicitBinding has returned
comment|// its parent's binding instead
if|if
condition|(
name|explicitBinding
operator|.
name|getInternalFactory
argument_list|()
operator|==
name|this
condition|)
block|{
name|errors
operator|.
name|withSource
argument_list|(
name|explicitBinding
operator|.
name|getSource
argument_list|()
argument_list|)
operator|.
name|exposedButNotBound
argument_list|(
name|key
argument_list|)
expr_stmt|;
return|return;
block|}
name|this
operator|.
name|delegate
operator|=
name|explicitBinding
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|get
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
throws|throws
name|ErrorsException
block|{
return|return
name|delegate
operator|.
name|getInternalFactory
argument_list|()
operator|.
name|get
argument_list|(
name|errors
argument_list|,
name|context
argument_list|,
name|dependency
argument_list|)
return|;
block|}
block|}
end_class

end_unit

