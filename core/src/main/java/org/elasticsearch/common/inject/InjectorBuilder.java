begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2006 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|BindingImpl
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
name|internal
operator|.
name|Stopwatch
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
name|Map
import|;
end_import

begin_comment
comment|/**  * Builds a tree of injectors. This is a primary injector, plus child injectors needed for each  * {@link Binder#newPrivateBinder() private environment}. The primary injector is not necessarily a  * top-level injector.  *<p>  * Injector construction happens in two phases.  *<ol>  *<li>Static building. In this phase, we interpret commands, create bindings, and inspect  * dependencies. During this phase, we hold a lock to ensure consistency with parent injectors.  * No user code is executed in this phase.</li>  *<li>Dynamic injection. In this phase, we call user code. We inject members that requested  * injection. This may require user's objects be created and their providers be called. And we  * create eager singletons. In this phase, user code may have started other threads. This phase  * is not executed for injectors created using {@link Stage#TOOL the tool stage}</li>  *</ol>  *  * @author crazybob@google.com (Bob Lee)  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|InjectorBuilder
class|class
name|InjectorBuilder
block|{
DECL|field|stopwatch
specifier|private
specifier|final
name|Stopwatch
name|stopwatch
init|=
operator|new
name|Stopwatch
argument_list|()
decl_stmt|;
DECL|field|errors
specifier|private
specifier|final
name|Errors
name|errors
init|=
operator|new
name|Errors
argument_list|()
decl_stmt|;
DECL|field|stage
specifier|private
name|Stage
name|stage
decl_stmt|;
DECL|field|initializer
specifier|private
specifier|final
name|Initializer
name|initializer
init|=
operator|new
name|Initializer
argument_list|()
decl_stmt|;
DECL|field|bindingProcesor
specifier|private
specifier|final
name|BindingProcessor
name|bindingProcesor
decl_stmt|;
DECL|field|injectionRequestProcessor
specifier|private
specifier|final
name|InjectionRequestProcessor
name|injectionRequestProcessor
decl_stmt|;
DECL|field|shellBuilder
specifier|private
specifier|final
name|InjectorShell
operator|.
name|Builder
name|shellBuilder
init|=
operator|new
name|InjectorShell
operator|.
name|Builder
argument_list|()
decl_stmt|;
DECL|field|shells
specifier|private
name|List
argument_list|<
name|InjectorShell
argument_list|>
name|shells
decl_stmt|;
DECL|method|InjectorBuilder
name|InjectorBuilder
parameter_list|()
block|{
name|injectionRequestProcessor
operator|=
operator|new
name|InjectionRequestProcessor
argument_list|(
name|errors
argument_list|,
name|initializer
argument_list|)
expr_stmt|;
name|bindingProcesor
operator|=
operator|new
name|BindingProcessor
argument_list|(
name|errors
argument_list|,
name|initializer
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets the stage for the created injector. If the stage is {@link Stage#PRODUCTION}, this class      * will eagerly load singletons.      */
DECL|method|stage
name|InjectorBuilder
name|stage
parameter_list|(
name|Stage
name|stage
parameter_list|)
block|{
name|shellBuilder
operator|.
name|stage
argument_list|(
name|stage
argument_list|)
expr_stmt|;
name|this
operator|.
name|stage
operator|=
name|stage
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|addModules
name|InjectorBuilder
name|addModules
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
name|modules
parameter_list|)
block|{
name|shellBuilder
operator|.
name|addModules
argument_list|(
name|modules
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
name|Injector
name|build
parameter_list|()
block|{
if|if
condition|(
name|shellBuilder
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Already built, builders are not reusable."
argument_list|)
throw|;
block|}
comment|// Synchronize while we're building up the bindings and other injector state. This ensures that
comment|// the JIT bindings in the parent injector don't change while we're being built
synchronized|synchronized
init|(
name|shellBuilder
operator|.
name|lock
argument_list|()
init|)
block|{
name|shells
operator|=
name|shellBuilder
operator|.
name|build
argument_list|(
name|initializer
argument_list|,
name|bindingProcesor
argument_list|,
name|stopwatch
argument_list|,
name|errors
argument_list|)
expr_stmt|;
name|stopwatch
operator|.
name|resetAndLog
argument_list|(
literal|"Injector construction"
argument_list|)
expr_stmt|;
name|initializeStatically
argument_list|()
expr_stmt|;
block|}
name|injectDynamically
argument_list|()
expr_stmt|;
return|return
name|primaryInjector
argument_list|()
return|;
block|}
comment|/**      * Initialize and validate everything.      */
DECL|method|initializeStatically
specifier|private
name|void
name|initializeStatically
parameter_list|()
block|{
name|bindingProcesor
operator|.
name|initializeBindings
argument_list|()
expr_stmt|;
name|stopwatch
operator|.
name|resetAndLog
argument_list|(
literal|"Binding initialization"
argument_list|)
expr_stmt|;
for|for
control|(
name|InjectorShell
name|shell
range|:
name|shells
control|)
block|{
name|shell
operator|.
name|getInjector
argument_list|()
operator|.
name|index
argument_list|()
expr_stmt|;
block|}
name|stopwatch
operator|.
name|resetAndLog
argument_list|(
literal|"Binding indexing"
argument_list|)
expr_stmt|;
name|injectionRequestProcessor
operator|.
name|process
argument_list|(
name|shells
argument_list|)
expr_stmt|;
name|stopwatch
operator|.
name|resetAndLog
argument_list|(
literal|"Collecting injection requests"
argument_list|)
expr_stmt|;
name|bindingProcesor
operator|.
name|runCreationListeners
argument_list|()
expr_stmt|;
name|stopwatch
operator|.
name|resetAndLog
argument_list|(
literal|"Binding validation"
argument_list|)
expr_stmt|;
name|injectionRequestProcessor
operator|.
name|validate
argument_list|()
expr_stmt|;
name|stopwatch
operator|.
name|resetAndLog
argument_list|(
literal|"Static validation"
argument_list|)
expr_stmt|;
name|initializer
operator|.
name|validateOustandingInjections
argument_list|(
name|errors
argument_list|)
expr_stmt|;
name|stopwatch
operator|.
name|resetAndLog
argument_list|(
literal|"Instance member validation"
argument_list|)
expr_stmt|;
operator|new
name|LookupProcessor
argument_list|(
name|errors
argument_list|)
operator|.
name|process
argument_list|(
name|shells
argument_list|)
expr_stmt|;
for|for
control|(
name|InjectorShell
name|shell
range|:
name|shells
control|)
block|{
operator|(
operator|(
name|DeferredLookups
operator|)
name|shell
operator|.
name|getInjector
argument_list|()
operator|.
name|lookups
operator|)
operator|.
name|initialize
argument_list|(
name|errors
argument_list|)
expr_stmt|;
block|}
name|stopwatch
operator|.
name|resetAndLog
argument_list|(
literal|"Provider verification"
argument_list|)
expr_stmt|;
for|for
control|(
name|InjectorShell
name|shell
range|:
name|shells
control|)
block|{
if|if
condition|(
operator|!
name|shell
operator|.
name|getElements
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Failed to execute "
operator|+
name|shell
operator|.
name|getElements
argument_list|()
argument_list|)
throw|;
block|}
block|}
name|errors
operator|.
name|throwCreationExceptionIfErrorsExist
argument_list|()
expr_stmt|;
block|}
comment|/**      * Returns the injector being constructed. This is not necessarily the root injector.      */
DECL|method|primaryInjector
specifier|private
name|Injector
name|primaryInjector
parameter_list|()
block|{
return|return
name|shells
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getInjector
argument_list|()
return|;
block|}
comment|/**      * Inject everything that can be injected. This method is intentionally not synchronized. If we      * locked while injecting members (ie. running user code), things would deadlock should the user      * code build a just-in-time binding from another thread.      */
DECL|method|injectDynamically
specifier|private
name|void
name|injectDynamically
parameter_list|()
block|{
name|injectionRequestProcessor
operator|.
name|injectMembers
argument_list|()
expr_stmt|;
name|stopwatch
operator|.
name|resetAndLog
argument_list|(
literal|"Static member injection"
argument_list|)
expr_stmt|;
name|initializer
operator|.
name|injectAll
argument_list|(
name|errors
argument_list|)
expr_stmt|;
name|stopwatch
operator|.
name|resetAndLog
argument_list|(
literal|"Instance injection"
argument_list|)
expr_stmt|;
name|errors
operator|.
name|throwCreationExceptionIfErrorsExist
argument_list|()
expr_stmt|;
for|for
control|(
name|InjectorShell
name|shell
range|:
name|shells
control|)
block|{
name|loadEagerSingletons
argument_list|(
name|shell
operator|.
name|getInjector
argument_list|()
argument_list|,
name|stage
argument_list|,
name|errors
argument_list|)
expr_stmt|;
block|}
name|stopwatch
operator|.
name|resetAndLog
argument_list|(
literal|"Preloading singletons"
argument_list|)
expr_stmt|;
name|errors
operator|.
name|throwCreationExceptionIfErrorsExist
argument_list|()
expr_stmt|;
block|}
comment|/**      * Loads eager singletons, or all singletons if we're in Stage.PRODUCTION. Bindings discovered      * while we're binding these singletons are not be eager.      */
DECL|method|loadEagerSingletons
specifier|public
name|void
name|loadEagerSingletons
parameter_list|(
name|InjectorImpl
name|injector
parameter_list|,
name|Stage
name|stage
parameter_list|,
name|Errors
name|errors
parameter_list|)
block|{
for|for
control|(
specifier|final
name|Binding
argument_list|<
name|?
argument_list|>
name|binding
range|:
name|injector
operator|.
name|state
operator|.
name|getExplicitBindingsThisLevel
argument_list|()
operator|.
name|values
argument_list|()
control|)
block|{
name|loadEagerSingletons
argument_list|(
name|injector
argument_list|,
name|stage
argument_list|,
name|errors
argument_list|,
operator|(
name|BindingImpl
argument_list|<
name|?
argument_list|>
operator|)
name|binding
argument_list|)
expr_stmt|;
block|}
for|for
control|(
specifier|final
name|Binding
argument_list|<
name|?
argument_list|>
name|binding
range|:
name|injector
operator|.
name|jitBindings
operator|.
name|values
argument_list|()
control|)
block|{
name|loadEagerSingletons
argument_list|(
name|injector
argument_list|,
name|stage
argument_list|,
name|errors
argument_list|,
operator|(
name|BindingImpl
argument_list|<
name|?
argument_list|>
operator|)
name|binding
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|loadEagerSingletons
specifier|private
name|void
name|loadEagerSingletons
parameter_list|(
name|InjectorImpl
name|injector
parameter_list|,
name|Stage
name|stage
parameter_list|,
specifier|final
name|Errors
name|errors
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
name|binding
operator|.
name|getScoping
argument_list|()
operator|.
name|isEagerSingleton
argument_list|(
name|stage
argument_list|)
condition|)
block|{
try|try
block|{
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
name|Dependency
argument_list|<
name|?
argument_list|>
name|dependency
init|=
name|Dependency
operator|.
name|get
argument_list|(
name|binding
operator|.
name|getKey
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Void
name|call
parameter_list|(
name|InternalContext
name|context
parameter_list|)
block|{
name|context
operator|.
name|setDependency
argument_list|(
name|dependency
argument_list|)
expr_stmt|;
name|Errors
name|errorsForBinding
init|=
name|errors
operator|.
name|withSource
argument_list|(
name|dependency
argument_list|)
decl_stmt|;
try|try
block|{
name|binding
operator|.
name|getInternalFactory
argument_list|()
operator|.
name|get
argument_list|(
name|errorsForBinding
argument_list|,
name|context
argument_list|,
name|dependency
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ErrorsException
name|e
parameter_list|)
block|{
name|errorsForBinding
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
finally|finally
block|{
name|context
operator|.
name|setDependency
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ErrorsException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
block|}
block|}
end_class

end_unit

