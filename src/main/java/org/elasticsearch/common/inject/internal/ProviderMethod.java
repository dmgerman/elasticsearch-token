begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ProviderWithDependencies
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|annotation
operator|.
name|Annotation
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * A provider that invokes a method and returns its result.  *  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|ProviderMethod
specifier|public
class|class
name|ProviderMethod
parameter_list|<
name|T
parameter_list|>
implements|implements
name|ProviderWithDependencies
argument_list|<
name|T
argument_list|>
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
DECL|field|scopeAnnotation
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|scopeAnnotation
decl_stmt|;
DECL|field|instance
specifier|private
specifier|final
name|Object
name|instance
decl_stmt|;
DECL|field|method
specifier|private
specifier|final
name|Method
name|method
decl_stmt|;
DECL|field|dependencies
specifier|private
specifier|final
name|ImmutableSet
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|dependencies
decl_stmt|;
DECL|field|parameterProviders
specifier|private
specifier|final
name|List
argument_list|<
name|Provider
argument_list|<
name|?
argument_list|>
argument_list|>
name|parameterProviders
decl_stmt|;
DECL|field|exposed
specifier|private
specifier|final
name|boolean
name|exposed
decl_stmt|;
comment|/**      * @param method the method to invoke. Its return type must be the same type as {@code key}.      */
DECL|method|ProviderMethod
name|ProviderMethod
parameter_list|(
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|,
name|Method
name|method
parameter_list|,
name|Object
name|instance
parameter_list|,
name|ImmutableSet
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|dependencies
parameter_list|,
name|List
argument_list|<
name|Provider
argument_list|<
name|?
argument_list|>
argument_list|>
name|parameterProviders
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|scopeAnnotation
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
name|scopeAnnotation
operator|=
name|scopeAnnotation
expr_stmt|;
name|this
operator|.
name|instance
operator|=
name|instance
expr_stmt|;
name|this
operator|.
name|dependencies
operator|=
name|dependencies
expr_stmt|;
name|this
operator|.
name|method
operator|=
name|method
expr_stmt|;
name|this
operator|.
name|parameterProviders
operator|=
name|parameterProviders
expr_stmt|;
name|this
operator|.
name|exposed
operator|=
name|method
operator|.
name|getAnnotation
argument_list|(
name|Exposed
operator|.
name|class
argument_list|)
operator|!=
literal|null
expr_stmt|;
name|method
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
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
DECL|method|getMethod
specifier|public
name|Method
name|getMethod
parameter_list|()
block|{
return|return
name|method
return|;
block|}
comment|// exposed for GIN
DECL|method|getInstance
specifier|public
name|Object
name|getInstance
parameter_list|()
block|{
return|return
name|instance
return|;
block|}
DECL|method|configure
specifier|public
name|void
name|configure
parameter_list|(
name|Binder
name|binder
parameter_list|)
block|{
name|binder
operator|=
name|binder
operator|.
name|withSource
argument_list|(
name|method
argument_list|)
expr_stmt|;
if|if
condition|(
name|scopeAnnotation
operator|!=
literal|null
condition|)
block|{
name|binder
operator|.
name|bind
argument_list|(
name|key
argument_list|)
operator|.
name|toProvider
argument_list|(
name|this
argument_list|)
operator|.
name|in
argument_list|(
name|scopeAnnotation
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|binder
operator|.
name|bind
argument_list|(
name|key
argument_list|)
operator|.
name|toProvider
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|exposed
condition|)
block|{
comment|// the cast is safe 'cause the only binder we have implements PrivateBinder. If there's a
comment|// misplaced @Exposed, calling this will add an error to the binder's error queue
operator|(
operator|(
name|PrivateBinder
operator|)
name|binder
operator|)
operator|.
name|expose
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|get
specifier|public
name|T
name|get
parameter_list|()
block|{
name|Object
index|[]
name|parameters
init|=
operator|new
name|Object
index|[
name|parameterProviders
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|parameters
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|parameters
index|[
name|i
index|]
operator|=
name|parameterProviders
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
try|try
block|{
comment|// We know this cast is safe because T is the method's return type.
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|,
literal|"UnnecessaryLocalVariable"
block|}
argument_list|)
name|T
name|result
init|=
operator|(
name|T
operator|)
name|method
operator|.
name|invoke
argument_list|(
name|instance
argument_list|,
name|parameters
argument_list|)
decl_stmt|;
return|return
name|result
return|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
DECL|method|getDependencies
specifier|public
name|Set
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|getDependencies
parameter_list|()
block|{
return|return
name|dependencies
return|;
block|}
block|}
end_class

end_unit

