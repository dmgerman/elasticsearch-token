begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2006 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationHandler
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
name|lang
operator|.
name|reflect
operator|.
name|Proxy
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
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
comment|/**  * Context of a dependency construction. Used to manage circular references.  *  * @author crazybob@google.com (Bob Lee)  */
end_comment

begin_class
DECL|class|ConstructionContext
specifier|public
class|class
name|ConstructionContext
parameter_list|<
name|T
parameter_list|>
block|{
DECL|field|currentReference
name|T
name|currentReference
decl_stmt|;
DECL|field|constructing
name|boolean
name|constructing
decl_stmt|;
DECL|field|invocationHandlers
name|List
argument_list|<
name|DelegatingInvocationHandler
argument_list|<
name|T
argument_list|>
argument_list|>
name|invocationHandlers
decl_stmt|;
DECL|method|getCurrentReference
specifier|public
name|T
name|getCurrentReference
parameter_list|()
block|{
return|return
name|currentReference
return|;
block|}
DECL|method|removeCurrentReference
specifier|public
name|void
name|removeCurrentReference
parameter_list|()
block|{
name|this
operator|.
name|currentReference
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|setCurrentReference
specifier|public
name|void
name|setCurrentReference
parameter_list|(
name|T
name|currentReference
parameter_list|)
block|{
name|this
operator|.
name|currentReference
operator|=
name|currentReference
expr_stmt|;
block|}
DECL|method|isConstructing
specifier|public
name|boolean
name|isConstructing
parameter_list|()
block|{
return|return
name|constructing
return|;
block|}
DECL|method|startConstruction
specifier|public
name|void
name|startConstruction
parameter_list|()
block|{
name|this
operator|.
name|constructing
operator|=
literal|true
expr_stmt|;
block|}
DECL|method|finishConstruction
specifier|public
name|void
name|finishConstruction
parameter_list|()
block|{
name|this
operator|.
name|constructing
operator|=
literal|false
expr_stmt|;
name|invocationHandlers
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|createProxy
specifier|public
name|Object
name|createProxy
parameter_list|(
name|Errors
name|errors
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|expectedType
parameter_list|)
throws|throws
name|ErrorsException
block|{
comment|// TODO: if I create a proxy which implements all the interfaces of
comment|// the implementation type, I'll be able to get away with one proxy
comment|// instance (as opposed to one per caller).
if|if
condition|(
operator|!
name|expectedType
operator|.
name|isInterface
argument_list|()
condition|)
block|{
throw|throw
name|errors
operator|.
name|cannotSatisfyCircularDependency
argument_list|(
name|expectedType
argument_list|)
operator|.
name|toException
argument_list|()
throw|;
block|}
if|if
condition|(
name|invocationHandlers
operator|==
literal|null
condition|)
block|{
name|invocationHandlers
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
block|}
name|DelegatingInvocationHandler
argument_list|<
name|T
argument_list|>
name|invocationHandler
init|=
operator|new
name|DelegatingInvocationHandler
argument_list|<>
argument_list|()
decl_stmt|;
name|invocationHandlers
operator|.
name|add
argument_list|(
name|invocationHandler
argument_list|)
expr_stmt|;
comment|// ES: Replace, since we don't use bytecode gen, just get the type class loader, or system if its null
comment|//ClassLoader classLoader = BytecodeGen.getClassLoader(expectedType);
name|ClassLoader
name|classLoader
init|=
name|expectedType
operator|.
name|getClassLoader
argument_list|()
operator|==
literal|null
condition|?
name|ClassLoader
operator|.
name|getSystemClassLoader
argument_list|()
else|:
name|expectedType
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
return|return
name|expectedType
operator|.
name|cast
argument_list|(
name|Proxy
operator|.
name|newProxyInstance
argument_list|(
name|classLoader
argument_list|,
operator|new
name|Class
index|[]
block|{
name|expectedType
block|}
argument_list|,
name|invocationHandler
argument_list|)
argument_list|)
return|;
block|}
DECL|method|setProxyDelegates
specifier|public
name|void
name|setProxyDelegates
parameter_list|(
name|T
name|delegate
parameter_list|)
block|{
if|if
condition|(
name|invocationHandlers
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|DelegatingInvocationHandler
argument_list|<
name|T
argument_list|>
name|handler
range|:
name|invocationHandlers
control|)
block|{
name|handler
operator|.
name|setDelegate
argument_list|(
name|delegate
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|DelegatingInvocationHandler
specifier|static
class|class
name|DelegatingInvocationHandler
parameter_list|<
name|T
parameter_list|>
implements|implements
name|InvocationHandler
block|{
DECL|field|delegate
name|T
name|delegate
decl_stmt|;
DECL|method|invoke
specifier|public
name|Object
name|invoke
parameter_list|(
name|Object
name|proxy
parameter_list|,
name|Method
name|method
parameter_list|,
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
if|if
condition|(
name|delegate
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"This is a proxy used to support"
operator|+
literal|" circular references involving constructors. The object we're"
operator|+
literal|" proxying is not constructed yet. Please wait until after"
operator|+
literal|" injection has completed to use this object."
argument_list|)
throw|;
block|}
try|try
block|{
comment|// This appears to be not test-covered
return|return
name|method
operator|.
name|invoke
argument_list|(
name|delegate
argument_list|,
name|args
argument_list|)
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
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
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
catch|catch
parameter_list|(
name|InvocationTargetException
name|e
parameter_list|)
block|{
throw|throw
name|e
operator|.
name|getTargetException
argument_list|()
throw|;
block|}
block|}
DECL|method|setDelegate
name|void
name|setDelegate
parameter_list|(
name|T
name|delegate
parameter_list|)
block|{
name|this
operator|.
name|delegate
operator|=
name|delegate
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

