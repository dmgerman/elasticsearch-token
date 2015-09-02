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
name|ImmutableSet
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
name|internal
operator|.
name|ErrorsException
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
name|InternalContext
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
name|InjectionListener
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
name|InjectionPoint
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
comment|/**  * Injects members of instances of a given type.  *  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|MembersInjectorImpl
class|class
name|MembersInjectorImpl
parameter_list|<
name|T
parameter_list|>
implements|implements
name|MembersInjector
argument_list|<
name|T
argument_list|>
block|{
DECL|field|typeLiteral
specifier|private
specifier|final
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|typeLiteral
decl_stmt|;
DECL|field|injector
specifier|private
specifier|final
name|InjectorImpl
name|injector
decl_stmt|;
DECL|field|memberInjectors
specifier|private
specifier|final
name|List
argument_list|<
name|SingleMemberInjector
argument_list|>
name|memberInjectors
decl_stmt|;
DECL|field|userMembersInjectors
specifier|private
specifier|final
name|List
argument_list|<
name|MembersInjector
argument_list|<
name|?
super|super
name|T
argument_list|>
argument_list|>
name|userMembersInjectors
decl_stmt|;
DECL|field|injectionListeners
specifier|private
specifier|final
name|List
argument_list|<
name|InjectionListener
argument_list|<
name|?
super|super
name|T
argument_list|>
argument_list|>
name|injectionListeners
decl_stmt|;
DECL|method|MembersInjectorImpl
name|MembersInjectorImpl
parameter_list|(
name|InjectorImpl
name|injector
parameter_list|,
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|typeLiteral
parameter_list|,
name|EncounterImpl
argument_list|<
name|T
argument_list|>
name|encounter
parameter_list|,
name|List
argument_list|<
name|SingleMemberInjector
argument_list|>
name|memberInjectors
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
name|typeLiteral
operator|=
name|typeLiteral
expr_stmt|;
name|this
operator|.
name|memberInjectors
operator|=
name|memberInjectors
expr_stmt|;
name|this
operator|.
name|userMembersInjectors
operator|=
name|encounter
operator|.
name|getMembersInjectors
argument_list|()
expr_stmt|;
name|this
operator|.
name|injectionListeners
operator|=
name|encounter
operator|.
name|getInjectionListeners
argument_list|()
expr_stmt|;
block|}
DECL|method|getMemberInjectors
specifier|public
name|List
argument_list|<
name|SingleMemberInjector
argument_list|>
name|getMemberInjectors
parameter_list|()
block|{
return|return
name|memberInjectors
return|;
block|}
annotation|@
name|Override
DECL|method|injectMembers
specifier|public
name|void
name|injectMembers
parameter_list|(
name|T
name|instance
parameter_list|)
block|{
name|Errors
name|errors
init|=
operator|new
name|Errors
argument_list|(
name|typeLiteral
argument_list|)
decl_stmt|;
try|try
block|{
name|injectAndNotify
argument_list|(
name|instance
argument_list|,
name|errors
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ErrorsException
name|e
parameter_list|)
block|{
name|errors
operator|.
name|merge
argument_list|(
name|e
operator|.
name|getErrors
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|errors
operator|.
name|throwProvisionExceptionIfErrorsExist
argument_list|()
expr_stmt|;
block|}
DECL|method|injectAndNotify
name|void
name|injectAndNotify
parameter_list|(
specifier|final
name|T
name|instance
parameter_list|,
specifier|final
name|Errors
name|errors
parameter_list|)
throws|throws
name|ErrorsException
block|{
if|if
condition|(
name|instance
operator|==
literal|null
condition|)
block|{
return|return;
block|}
name|injector
operator|.
name|callInContext
argument_list|(
operator|new
name|ContextualCallable
argument_list|<
name|Void
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|(
name|InternalContext
name|context
parameter_list|)
throws|throws
name|ErrorsException
block|{
name|injectMembers
argument_list|(
name|instance
argument_list|,
name|errors
argument_list|,
name|context
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|notifyListeners
argument_list|(
name|instance
argument_list|,
name|errors
argument_list|)
expr_stmt|;
block|}
DECL|method|notifyListeners
name|void
name|notifyListeners
parameter_list|(
name|T
name|instance
parameter_list|,
name|Errors
name|errors
parameter_list|)
throws|throws
name|ErrorsException
block|{
name|int
name|numErrorsBefore
init|=
name|errors
operator|.
name|size
argument_list|()
decl_stmt|;
for|for
control|(
name|InjectionListener
argument_list|<
name|?
super|super
name|T
argument_list|>
name|injectionListener
range|:
name|injectionListeners
control|)
block|{
try|try
block|{
name|injectionListener
operator|.
name|afterInjection
argument_list|(
name|instance
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|errors
operator|.
name|errorNotifyingInjectionListener
argument_list|(
name|injectionListener
argument_list|,
name|typeLiteral
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
name|errors
operator|.
name|throwIfNewErrors
argument_list|(
name|numErrorsBefore
argument_list|)
expr_stmt|;
block|}
DECL|method|injectMembers
name|void
name|injectMembers
parameter_list|(
name|T
name|t
parameter_list|,
name|Errors
name|errors
parameter_list|,
name|InternalContext
name|context
parameter_list|)
block|{
comment|// optimization: use manual for/each to save allocating an iterator here
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|size
init|=
name|memberInjectors
operator|.
name|size
argument_list|()
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|memberInjectors
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|inject
argument_list|(
name|errors
argument_list|,
name|context
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
comment|// optimization: use manual for/each to save allocating an iterator here
for|for
control|(
name|int
name|i
init|=
literal|0
init|,
name|size
init|=
name|userMembersInjectors
operator|.
name|size
argument_list|()
init|;
name|i
operator|<
name|size
condition|;
name|i
operator|++
control|)
block|{
name|MembersInjector
argument_list|<
name|?
super|super
name|T
argument_list|>
name|userMembersInjector
init|=
name|userMembersInjectors
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
try|try
block|{
name|userMembersInjector
operator|.
name|injectMembers
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|RuntimeException
name|e
parameter_list|)
block|{
name|errors
operator|.
name|errorInUserInjector
argument_list|(
name|userMembersInjector
argument_list|,
name|typeLiteral
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
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
literal|"MembersInjector<"
operator|+
name|typeLiteral
operator|+
literal|">"
return|;
block|}
DECL|method|getInjectionPoints
specifier|public
name|ImmutableSet
argument_list|<
name|InjectionPoint
argument_list|>
name|getInjectionPoints
parameter_list|()
block|{
name|ImmutableSet
operator|.
name|Builder
argument_list|<
name|InjectionPoint
argument_list|>
name|builder
init|=
name|ImmutableSet
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|SingleMemberInjector
name|memberInjector
range|:
name|memberInjectors
control|)
block|{
name|builder
operator|.
name|add
argument_list|(
name|memberInjector
operator|.
name|getInjectionPoint
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
block|}
end_class

end_unit

