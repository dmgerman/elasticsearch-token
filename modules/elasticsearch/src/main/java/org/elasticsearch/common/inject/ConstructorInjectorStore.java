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
name|FailableCache
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

begin_comment
comment|/**  * Constructor injectors by type.  *  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|ConstructorInjectorStore
class|class
name|ConstructorInjectorStore
block|{
DECL|field|injector
specifier|private
specifier|final
name|InjectorImpl
name|injector
decl_stmt|;
DECL|field|cache
specifier|private
specifier|final
name|FailableCache
argument_list|<
name|TypeLiteral
argument_list|<
name|?
argument_list|>
argument_list|,
name|ConstructorInjector
argument_list|<
name|?
argument_list|>
argument_list|>
name|cache
init|=
operator|new
name|FailableCache
argument_list|<
name|TypeLiteral
argument_list|<
name|?
argument_list|>
argument_list|,
name|ConstructorInjector
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|protected
name|ConstructorInjector
argument_list|<
name|?
argument_list|>
name|create
parameter_list|(
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|type
parameter_list|,
name|Errors
name|errors
parameter_list|)
throws|throws
name|ErrorsException
block|{
return|return
name|createConstructor
argument_list|(
name|type
argument_list|,
name|errors
argument_list|)
return|;
block|}
block|}
empty_stmt|;
DECL|method|ConstructorInjectorStore
name|ConstructorInjectorStore
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
comment|/**      * Returns a new complete constructor injector with injection listeners registered.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
comment|// the ConstructorInjector type always agrees with the passed type
DECL|method|get
specifier|public
parameter_list|<
name|T
parameter_list|>
name|ConstructorInjector
argument_list|<
name|T
argument_list|>
name|get
parameter_list|(
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|key
parameter_list|,
name|Errors
name|errors
parameter_list|)
throws|throws
name|ErrorsException
block|{
return|return
operator|(
name|ConstructorInjector
argument_list|<
name|T
argument_list|>
operator|)
name|cache
operator|.
name|get
argument_list|(
name|key
argument_list|,
name|errors
argument_list|)
return|;
block|}
DECL|method|createConstructor
specifier|private
parameter_list|<
name|T
parameter_list|>
name|ConstructorInjector
argument_list|<
name|T
argument_list|>
name|createConstructor
parameter_list|(
name|TypeLiteral
argument_list|<
name|T
argument_list|>
name|type
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
name|InjectionPoint
name|injectionPoint
decl_stmt|;
try|try
block|{
name|injectionPoint
operator|=
name|InjectionPoint
operator|.
name|forConstructorOf
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ConfigurationException
name|e
parameter_list|)
block|{
name|errors
operator|.
name|merge
argument_list|(
name|e
operator|.
name|getErrorMessages
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
name|errors
operator|.
name|toException
argument_list|()
throw|;
block|}
name|SingleParameterInjector
argument_list|<
name|?
argument_list|>
index|[]
name|constructorParameterInjectors
init|=
name|injector
operator|.
name|getParametersInjectors
argument_list|(
name|injectionPoint
operator|.
name|getDependencies
argument_list|()
argument_list|,
name|errors
argument_list|)
decl_stmt|;
name|MembersInjectorImpl
argument_list|<
name|T
argument_list|>
name|membersInjector
init|=
name|injector
operator|.
name|membersInjectorStore
operator|.
name|get
argument_list|(
name|type
argument_list|,
name|errors
argument_list|)
decl_stmt|;
name|ConstructionProxyFactory
argument_list|<
name|T
argument_list|>
name|factory
init|=
operator|new
name|DefaultConstructionProxyFactory
argument_list|<
name|T
argument_list|>
argument_list|(
name|injectionPoint
argument_list|)
decl_stmt|;
name|errors
operator|.
name|throwIfNewErrors
argument_list|(
name|numErrorsBefore
argument_list|)
expr_stmt|;
return|return
operator|new
name|ConstructorInjector
argument_list|<
name|T
argument_list|>
argument_list|(
name|membersInjector
operator|.
name|getInjectionPoints
argument_list|()
argument_list|,
name|factory
operator|.
name|create
argument_list|()
argument_list|,
name|constructorParameterInjectors
argument_list|,
name|membersInjector
argument_list|)
return|;
block|}
block|}
end_class

end_unit

