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
name|*
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
comment|/**  * Handles {@link Binder#bind} and {@link Binder#bindConstant} elements.  *  * @author crazybob@google.com (Bob Lee)  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|BindingProcessor
class|class
name|BindingProcessor
extends|extends
name|AbstractProcessor
block|{
DECL|field|creationListeners
specifier|private
specifier|final
name|List
argument_list|<
name|CreationListener
argument_list|>
name|creationListeners
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
DECL|field|initializer
specifier|private
specifier|final
name|Initializer
name|initializer
decl_stmt|;
DECL|field|uninitializedBindings
specifier|private
specifier|final
name|List
argument_list|<
name|Runnable
argument_list|>
name|uninitializedBindings
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
DECL|method|BindingProcessor
name|BindingProcessor
parameter_list|(
name|Errors
name|errors
parameter_list|,
name|Initializer
name|initializer
parameter_list|)
block|{
name|super
argument_list|(
name|errors
argument_list|)
expr_stmt|;
name|this
operator|.
name|initializer
operator|=
name|initializer
expr_stmt|;
block|}
DECL|method|visit
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|Boolean
name|visit
parameter_list|(
name|Binding
argument_list|<
name|T
argument_list|>
name|command
parameter_list|)
block|{
specifier|final
name|Object
name|source
init|=
name|command
operator|.
name|getSource
argument_list|()
decl_stmt|;
if|if
condition|(
name|Void
operator|.
name|class
operator|.
name|equals
argument_list|(
name|command
operator|.
name|getKey
argument_list|()
operator|.
name|getRawType
argument_list|()
argument_list|)
condition|)
block|{
if|if
condition|(
name|command
operator|instanceof
name|ProviderInstanceBinding
operator|&&
operator|(
operator|(
name|ProviderInstanceBinding
operator|)
name|command
operator|)
operator|.
name|getProviderInstance
argument_list|()
operator|instanceof
name|ProviderMethod
condition|)
block|{
name|errors
operator|.
name|voidProviderMethod
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|errors
operator|.
name|missingConstantValues
argument_list|()
expr_stmt|;
block|}
return|return
literal|true
return|;
block|}
specifier|final
name|Key
argument_list|<
name|T
argument_list|>
name|key
init|=
name|command
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
super|super
name|T
argument_list|>
name|rawType
init|=
name|key
operator|.
name|getTypeLiteral
argument_list|()
operator|.
name|getRawType
argument_list|()
decl_stmt|;
if|if
condition|(
name|rawType
operator|==
name|Provider
operator|.
name|class
condition|)
block|{
name|errors
operator|.
name|bindingToProvider
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
name|validateKey
argument_list|(
name|command
operator|.
name|getSource
argument_list|()
argument_list|,
name|command
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|Scoping
name|scoping
init|=
name|Scopes
operator|.
name|makeInjectable
argument_list|(
operator|(
operator|(
name|BindingImpl
argument_list|<
name|?
argument_list|>
operator|)
name|command
operator|)
operator|.
name|getScoping
argument_list|()
argument_list|,
name|injector
argument_list|,
name|errors
argument_list|)
decl_stmt|;
name|command
operator|.
name|acceptTargetVisitor
argument_list|(
operator|new
name|BindingTargetVisitor
argument_list|<
name|T
argument_list|,
name|Void
argument_list|>
argument_list|()
block|{
specifier|public
name|Void
name|visit
parameter_list|(
name|InstanceBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|binding
parameter_list|)
block|{
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|injectionPoints
init|=
name|binding
operator|.
name|getInjectionPoints
argument_list|()
decl_stmt|;
name|T
name|instance
init|=
name|binding
operator|.
name|getInstance
argument_list|()
decl_stmt|;
name|Initializable
argument_list|<
name|T
argument_list|>
name|ref
init|=
name|initializer
operator|.
name|requestInjection
argument_list|(
name|injector
argument_list|,
name|instance
argument_list|,
name|source
argument_list|,
name|injectionPoints
argument_list|)
decl_stmt|;
name|ConstantFactory
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|factory
init|=
operator|new
name|ConstantFactory
argument_list|<
name|T
argument_list|>
argument_list|(
name|ref
argument_list|)
decl_stmt|;
name|InternalFactory
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|scopedFactory
init|=
name|Scopes
operator|.
name|scope
argument_list|(
name|key
argument_list|,
name|injector
argument_list|,
name|factory
argument_list|,
name|scoping
argument_list|)
decl_stmt|;
name|putBinding
argument_list|(
operator|new
name|InstanceBindingImpl
argument_list|<
name|T
argument_list|>
argument_list|(
name|injector
argument_list|,
name|key
argument_list|,
name|source
argument_list|,
name|scopedFactory
argument_list|,
name|injectionPoints
argument_list|,
name|instance
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|public
name|Void
name|visit
parameter_list|(
name|ProviderInstanceBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|binding
parameter_list|)
block|{
name|Provider
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|provider
init|=
name|binding
operator|.
name|getProviderInstance
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|injectionPoints
init|=
name|binding
operator|.
name|getInjectionPoints
argument_list|()
decl_stmt|;
name|Initializable
argument_list|<
name|Provider
argument_list|<
name|?
extends|extends
name|T
argument_list|>
argument_list|>
name|initializable
init|=
name|initializer
operator|.
expr|<
name|Provider
argument_list|<
name|?
extends|extends
name|T
argument_list|>
operator|>
name|requestInjection
argument_list|(
name|injector
argument_list|,
name|provider
argument_list|,
name|source
argument_list|,
name|injectionPoints
argument_list|)
decl_stmt|;
name|InternalFactory
argument_list|<
name|T
argument_list|>
name|factory
init|=
operator|new
name|InternalFactoryToProviderAdapter
argument_list|<
name|T
argument_list|>
argument_list|(
name|initializable
argument_list|,
name|source
argument_list|)
decl_stmt|;
name|InternalFactory
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|scopedFactory
init|=
name|Scopes
operator|.
name|scope
argument_list|(
name|key
argument_list|,
name|injector
argument_list|,
name|factory
argument_list|,
name|scoping
argument_list|)
decl_stmt|;
name|putBinding
argument_list|(
operator|new
name|ProviderInstanceBindingImpl
argument_list|<
name|T
argument_list|>
argument_list|(
name|injector
argument_list|,
name|key
argument_list|,
name|source
argument_list|,
name|scopedFactory
argument_list|,
name|scoping
argument_list|,
name|provider
argument_list|,
name|injectionPoints
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|public
name|Void
name|visit
parameter_list|(
name|ProviderKeyBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|binding
parameter_list|)
block|{
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
init|=
name|binding
operator|.
name|getProviderKey
argument_list|()
decl_stmt|;
name|BoundProviderFactory
argument_list|<
name|T
argument_list|>
name|boundProviderFactory
init|=
operator|new
name|BoundProviderFactory
argument_list|<
name|T
argument_list|>
argument_list|(
name|injector
argument_list|,
name|providerKey
argument_list|,
name|source
argument_list|)
decl_stmt|;
name|creationListeners
operator|.
name|add
argument_list|(
name|boundProviderFactory
argument_list|)
expr_stmt|;
name|InternalFactory
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|scopedFactory
init|=
name|Scopes
operator|.
name|scope
argument_list|(
name|key
argument_list|,
name|injector
argument_list|,
operator|(
name|InternalFactory
argument_list|<
name|?
extends|extends
name|T
argument_list|>
operator|)
name|boundProviderFactory
argument_list|,
name|scoping
argument_list|)
decl_stmt|;
name|putBinding
argument_list|(
operator|new
name|LinkedProviderBindingImpl
argument_list|<
name|T
argument_list|>
argument_list|(
name|injector
argument_list|,
name|key
argument_list|,
name|source
argument_list|,
name|scopedFactory
argument_list|,
name|scoping
argument_list|,
name|providerKey
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|public
name|Void
name|visit
parameter_list|(
name|LinkedKeyBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|binding
parameter_list|)
block|{
name|Key
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|linkedKey
init|=
name|binding
operator|.
name|getLinkedKey
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|.
name|equals
argument_list|(
name|linkedKey
argument_list|)
condition|)
block|{
name|errors
operator|.
name|recursiveBinding
argument_list|()
expr_stmt|;
block|}
name|FactoryProxy
argument_list|<
name|T
argument_list|>
name|factory
init|=
operator|new
name|FactoryProxy
argument_list|<
name|T
argument_list|>
argument_list|(
name|injector
argument_list|,
name|key
argument_list|,
name|linkedKey
argument_list|,
name|source
argument_list|)
decl_stmt|;
name|creationListeners
operator|.
name|add
argument_list|(
name|factory
argument_list|)
expr_stmt|;
name|InternalFactory
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|scopedFactory
init|=
name|Scopes
operator|.
name|scope
argument_list|(
name|key
argument_list|,
name|injector
argument_list|,
name|factory
argument_list|,
name|scoping
argument_list|)
decl_stmt|;
name|putBinding
argument_list|(
operator|new
name|LinkedBindingImpl
argument_list|<
name|T
argument_list|>
argument_list|(
name|injector
argument_list|,
name|key
argument_list|,
name|source
argument_list|,
name|scopedFactory
argument_list|,
name|scoping
argument_list|,
name|linkedKey
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|public
name|Void
name|visit
parameter_list|(
name|UntargettedBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|untargetted
parameter_list|)
block|{
comment|// Error: Missing implementation.
comment|// Example: bind(Date.class).annotatedWith(Red.class);
comment|// We can't assume abstract types aren't injectable. They may have an
comment|// @ImplementedBy annotation or something.
if|if
condition|(
name|key
operator|.
name|hasAnnotationType
argument_list|()
condition|)
block|{
name|errors
operator|.
name|missingImplementation
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|putBinding
argument_list|(
name|invalidBinding
argument_list|(
name|injector
argument_list|,
name|key
argument_list|,
name|source
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
comment|// This cast is safe after the preceeding check.
specifier|final
name|BindingImpl
argument_list|<
name|T
argument_list|>
name|binding
decl_stmt|;
try|try
block|{
name|binding
operator|=
name|injector
operator|.
name|createUnitializedBinding
argument_list|(
name|key
argument_list|,
name|scoping
argument_list|,
name|source
argument_list|,
name|errors
argument_list|)
expr_stmt|;
name|putBinding
argument_list|(
name|binding
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
name|putBinding
argument_list|(
name|invalidBinding
argument_list|(
name|injector
argument_list|,
name|key
argument_list|,
name|source
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
name|uninitializedBindings
operator|.
name|add
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
operator|(
operator|(
name|InjectorImpl
operator|)
name|binding
operator|.
name|getInjector
argument_list|()
operator|)
operator|.
name|initializeBinding
argument_list|(
name|binding
argument_list|,
name|errors
operator|.
name|withSource
argument_list|(
name|source
argument_list|)
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
block|}
block|}
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
specifier|public
name|Void
name|visit
parameter_list|(
name|ExposedBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|binding
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot apply a non-module element"
argument_list|)
throw|;
block|}
specifier|public
name|Void
name|visit
parameter_list|(
name|ConvertedConstantBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|binding
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot apply a non-module element"
argument_list|)
throw|;
block|}
specifier|public
name|Void
name|visit
parameter_list|(
name|ConstructorBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|binding
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot apply a non-module element"
argument_list|)
throw|;
block|}
specifier|public
name|Void
name|visit
parameter_list|(
name|ProviderBinding
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|binding
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot apply a non-module element"
argument_list|)
throw|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
DECL|method|visit
annotation|@
name|Override
specifier|public
name|Boolean
name|visit
parameter_list|(
name|PrivateElements
name|privateElements
parameter_list|)
block|{
for|for
control|(
name|Key
argument_list|<
name|?
argument_list|>
name|key
range|:
name|privateElements
operator|.
name|getExposedKeys
argument_list|()
control|)
block|{
name|bindExposed
argument_list|(
name|privateElements
argument_list|,
name|key
argument_list|)
expr_stmt|;
block|}
return|return
literal|false
return|;
comment|// leave the private elements for the PrivateElementsProcessor to handle
block|}
DECL|method|bindExposed
specifier|private
parameter_list|<
name|T
parameter_list|>
name|void
name|bindExposed
parameter_list|(
name|PrivateElements
name|privateElements
parameter_list|,
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|)
block|{
name|ExposedKeyFactory
argument_list|<
name|T
argument_list|>
name|exposedKeyFactory
init|=
operator|new
name|ExposedKeyFactory
argument_list|<
name|T
argument_list|>
argument_list|(
name|key
argument_list|,
name|privateElements
argument_list|)
decl_stmt|;
name|creationListeners
operator|.
name|add
argument_list|(
name|exposedKeyFactory
argument_list|)
expr_stmt|;
name|putBinding
argument_list|(
operator|new
name|ExposedBindingImpl
argument_list|<
name|T
argument_list|>
argument_list|(
name|injector
argument_list|,
name|privateElements
operator|.
name|getExposedSource
argument_list|(
name|key
argument_list|)
argument_list|,
name|key
argument_list|,
name|exposedKeyFactory
argument_list|,
name|privateElements
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|validateKey
specifier|private
parameter_list|<
name|T
parameter_list|>
name|void
name|validateKey
parameter_list|(
name|Object
name|source
parameter_list|,
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|)
block|{
name|Annotations
operator|.
name|checkForMisplacedScopeAnnotations
argument_list|(
name|key
operator|.
name|getRawType
argument_list|()
argument_list|,
name|source
argument_list|,
name|errors
argument_list|)
expr_stmt|;
block|}
DECL|method|invalidBinding
parameter_list|<
name|T
parameter_list|>
name|UntargettedBindingImpl
argument_list|<
name|T
argument_list|>
name|invalidBinding
parameter_list|(
name|InjectorImpl
name|injector
parameter_list|,
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|,
name|Object
name|source
parameter_list|)
block|{
return|return
operator|new
name|UntargettedBindingImpl
argument_list|<
name|T
argument_list|>
argument_list|(
name|injector
argument_list|,
name|key
argument_list|,
name|source
argument_list|)
return|;
block|}
DECL|method|initializeBindings
specifier|public
name|void
name|initializeBindings
parameter_list|()
block|{
for|for
control|(
name|Runnable
name|initializer
range|:
name|uninitializedBindings
control|)
block|{
name|initializer
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|runCreationListeners
specifier|public
name|void
name|runCreationListeners
parameter_list|()
block|{
for|for
control|(
name|CreationListener
name|creationListener
range|:
name|creationListeners
control|)
block|{
name|creationListener
operator|.
name|notify
argument_list|(
name|errors
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|putBinding
specifier|private
name|void
name|putBinding
parameter_list|(
name|BindingImpl
argument_list|<
name|?
argument_list|>
name|binding
parameter_list|)
block|{
name|Key
argument_list|<
name|?
argument_list|>
name|key
init|=
name|binding
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|rawType
init|=
name|key
operator|.
name|getRawType
argument_list|()
decl_stmt|;
if|if
condition|(
name|FORBIDDEN_TYPES
operator|.
name|contains
argument_list|(
name|rawType
argument_list|)
condition|)
block|{
name|errors
operator|.
name|cannotBindToGuiceType
argument_list|(
name|rawType
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|Binding
argument_list|<
name|?
argument_list|>
name|original
init|=
name|injector
operator|.
name|state
operator|.
name|getExplicitBinding
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|original
operator|!=
literal|null
operator|&&
operator|!
name|isOkayDuplicate
argument_list|(
name|original
argument_list|,
name|binding
argument_list|)
condition|)
block|{
name|errors
operator|.
name|bindingAlreadySet
argument_list|(
name|key
argument_list|,
name|original
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
comment|// prevent the parent from creating a JIT binding for this key
name|injector
operator|.
name|state
operator|.
name|parent
argument_list|()
operator|.
name|blacklist
argument_list|(
name|key
argument_list|)
expr_stmt|;
name|injector
operator|.
name|state
operator|.
name|putBinding
argument_list|(
name|key
argument_list|,
name|binding
argument_list|)
expr_stmt|;
block|}
comment|/**      * We tolerate duplicate bindings only if one exposes the other.      *      * @param original the binding in the parent injector (candidate for an exposing binding)      * @param binding  the binding to check (candidate for the exposed binding)      */
DECL|method|isOkayDuplicate
specifier|private
name|boolean
name|isOkayDuplicate
parameter_list|(
name|Binding
argument_list|<
name|?
argument_list|>
name|original
parameter_list|,
name|BindingImpl
argument_list|<
name|?
argument_list|>
name|binding
parameter_list|)
block|{
if|if
condition|(
name|original
operator|instanceof
name|ExposedBindingImpl
condition|)
block|{
name|ExposedBindingImpl
name|exposed
init|=
operator|(
name|ExposedBindingImpl
operator|)
name|original
decl_stmt|;
name|InjectorImpl
name|exposedFrom
init|=
operator|(
name|InjectorImpl
operator|)
name|exposed
operator|.
name|getPrivateElements
argument_list|()
operator|.
name|getInjector
argument_list|()
decl_stmt|;
return|return
operator|(
name|exposedFrom
operator|==
name|binding
operator|.
name|getInjector
argument_list|()
operator|)
return|;
block|}
return|return
literal|false
return|;
block|}
comment|// It's unfortunate that we have to maintain a blacklist of specific
comment|// classes, but we can't easily block the whole package because of
comment|// all our unit tests.
DECL|field|FORBIDDEN_TYPES
specifier|private
specifier|static
specifier|final
name|Set
argument_list|<
name|Class
argument_list|<
name|?
argument_list|>
argument_list|>
name|FORBIDDEN_TYPES
init|=
name|ImmutableSet
operator|.
name|of
argument_list|(
name|AbstractModule
operator|.
name|class
argument_list|,
name|Binder
operator|.
name|class
argument_list|,
name|Binding
operator|.
name|class
argument_list|,
name|Injector
operator|.
name|class
argument_list|,
name|Key
operator|.
name|class
argument_list|,
name|MembersInjector
operator|.
name|class
argument_list|,
name|Module
operator|.
name|class
argument_list|,
name|Provider
operator|.
name|class
argument_list|,
name|Scope
operator|.
name|class
argument_list|,
name|TypeLiteral
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// TODO(jessewilson): fix BuiltInModule, then add Stage
DECL|interface|CreationListener
interface|interface
name|CreationListener
block|{
DECL|method|notify
name|void
name|notify
parameter_list|(
name|Errors
name|errors
parameter_list|)
function_decl|;
block|}
block|}
end_class

end_unit

