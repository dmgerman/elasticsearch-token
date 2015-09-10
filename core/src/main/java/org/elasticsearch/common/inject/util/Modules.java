begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject.util
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|util
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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
name|AbstractModule
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
name|Binder
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
name|Binding
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
name|Module
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
name|PrivateBinder
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
name|Scope
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
name|DefaultBindingScopingVisitor
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
name|DefaultElementVisitor
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
name|Elements
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
name|ScopeBinding
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
comment|/**  * Static utility methods for creating and working with instances of {@link Module}.  *  * @author jessewilson@google.com (Jesse Wilson)  * @since 2.0  */
end_comment

begin_class
DECL|class|Modules
specifier|public
specifier|final
class|class
name|Modules
block|{
DECL|method|Modules
specifier|private
name|Modules
parameter_list|()
block|{     }
DECL|field|EMPTY_MODULE
specifier|public
specifier|static
specifier|final
name|Module
name|EMPTY_MODULE
init|=
operator|new
name|Module
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|configure
parameter_list|(
name|Binder
name|binder
parameter_list|)
block|{         }
block|}
decl_stmt|;
comment|/**      * Returns a builder that creates a module that overlays override modules over the given      * modules. If a key is bound in both sets of modules, only the binding from the override modules      * is kept. This can be used to replace the bindings of a production module with test bindings:      *<pre>      * Module functionalTestModule      *     = Modules.override(new ProductionModule()).with(new TestModule());      *</pre>      *<p/>      *<p>Prefer to write smaller modules that can be reused and tested without overrides.      *      * @param modules the modules whose bindings are open to be overridden      */
DECL|method|override
specifier|public
specifier|static
name|OverriddenModuleBuilder
name|override
parameter_list|(
name|Module
modifier|...
name|modules
parameter_list|)
block|{
return|return
operator|new
name|RealOverriddenModuleBuilder
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|modules
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Returns a builder that creates a module that overlays override modules over the given      * modules. If a key is bound in both sets of modules, only the binding from the override modules      * is kept. This can be used to replace the bindings of a production module with test bindings:      *<pre>      * Module functionalTestModule      *     = Modules.override(getProductionModules()).with(getTestModules());      *</pre>      *<p/>      *<p>Prefer to write smaller modules that can be reused and tested without overrides.      *      * @param modules the modules whose bindings are open to be overridden      */
DECL|method|override
specifier|public
specifier|static
name|OverriddenModuleBuilder
name|override
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
return|return
operator|new
name|RealOverriddenModuleBuilder
argument_list|(
name|modules
argument_list|)
return|;
block|}
comment|/**      * Returns a new module that installs all of {@code modules}.      */
DECL|method|combine
specifier|public
specifier|static
name|Module
name|combine
parameter_list|(
name|Module
modifier|...
name|modules
parameter_list|)
block|{
return|return
name|combine
argument_list|(
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|modules
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Returns a new module that installs all of {@code modules}.      */
DECL|method|combine
specifier|public
specifier|static
name|Module
name|combine
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
comment|// TODO: infer type once JI-9019884 is fixed
specifier|final
name|Set
argument_list|<
name|Module
argument_list|>
name|modulesSet
init|=
name|ImmutableSet
operator|.
expr|<
name|Module
operator|>
name|copyOf
argument_list|(
name|modules
argument_list|)
decl_stmt|;
return|return
operator|new
name|Module
argument_list|()
block|{
annotation|@
name|Override
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
name|skipSources
argument_list|(
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Module
name|module
range|:
name|modulesSet
control|)
block|{
name|binder
operator|.
name|install
argument_list|(
name|module
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|;
block|}
comment|/**      * See the EDSL example at {@link Modules#override(Module[]) override()}.      */
DECL|interface|OverriddenModuleBuilder
specifier|public
interface|interface
name|OverriddenModuleBuilder
block|{
comment|/**          * See the EDSL example at {@link Modules#override(Module[]) override()}.          */
DECL|method|with
name|Module
name|with
parameter_list|(
name|Module
modifier|...
name|overrides
parameter_list|)
function_decl|;
comment|/**          * See the EDSL example at {@link Modules#override(Module[]) override()}.          */
DECL|method|with
name|Module
name|with
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
name|overrides
parameter_list|)
function_decl|;
block|}
DECL|class|RealOverriddenModuleBuilder
specifier|private
specifier|static
specifier|final
class|class
name|RealOverriddenModuleBuilder
implements|implements
name|OverriddenModuleBuilder
block|{
DECL|field|baseModules
specifier|private
specifier|final
name|ImmutableSet
argument_list|<
name|Module
argument_list|>
name|baseModules
decl_stmt|;
DECL|method|RealOverriddenModuleBuilder
specifier|private
name|RealOverriddenModuleBuilder
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
name|baseModules
parameter_list|)
block|{
comment|// TODO: infer type once JI-9019884 is fixed
name|this
operator|.
name|baseModules
operator|=
name|ImmutableSet
operator|.
expr|<
name|Module
operator|>
name|copyOf
argument_list|(
name|baseModules
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|with
specifier|public
name|Module
name|with
parameter_list|(
name|Module
modifier|...
name|overrides
parameter_list|)
block|{
return|return
name|with
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|overrides
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|with
specifier|public
name|Module
name|with
parameter_list|(
specifier|final
name|Iterable
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
name|overrides
parameter_list|)
block|{
return|return
operator|new
name|AbstractModule
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|configure
parameter_list|()
block|{
specifier|final
name|List
argument_list|<
name|Element
argument_list|>
name|elements
init|=
name|Elements
operator|.
name|getElements
argument_list|(
name|baseModules
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|Element
argument_list|>
name|overrideElements
init|=
name|Elements
operator|.
name|getElements
argument_list|(
name|overrides
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|Key
argument_list|>
name|overriddenKeys
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
argument_list|>
name|overridesScopeAnnotations
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
comment|// execute the overrides module, keeping track of which keys and scopes are bound
operator|new
name|ModuleWriter
argument_list|(
name|binder
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|Void
name|visit
parameter_list|(
name|Binding
argument_list|<
name|T
argument_list|>
name|binding
parameter_list|)
block|{
name|overriddenKeys
operator|.
name|add
argument_list|(
name|binding
operator|.
name|getKey
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|visit
argument_list|(
name|binding
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Void
name|visit
parameter_list|(
name|ScopeBinding
name|scopeBinding
parameter_list|)
block|{
name|overridesScopeAnnotations
operator|.
name|add
argument_list|(
name|scopeBinding
operator|.
name|getAnnotationType
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|visit
argument_list|(
name|scopeBinding
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Void
name|visit
parameter_list|(
name|PrivateElements
name|privateElements
parameter_list|)
block|{
name|overriddenKeys
operator|.
name|addAll
argument_list|(
name|privateElements
operator|.
name|getExposedKeys
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|super
operator|.
name|visit
argument_list|(
name|privateElements
argument_list|)
return|;
block|}
block|}
operator|.
name|writeAll
argument_list|(
name|overrideElements
argument_list|)
expr_stmt|;
comment|// execute the original module, skipping all scopes and overridden keys. We only skip each
comment|// overridden binding once so things still blow up if the module binds the same thing
comment|// multiple times.
specifier|final
name|Map
argument_list|<
name|Scope
argument_list|,
name|Object
argument_list|>
name|scopeInstancesInUse
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|List
argument_list|<
name|ScopeBinding
argument_list|>
name|scopeBindings
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
operator|new
name|ModuleWriter
argument_list|(
name|binder
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|>
name|Void
name|visit
parameter_list|(
name|Binding
argument_list|<
name|T
argument_list|>
name|binding
parameter_list|)
block|{
if|if
condition|(
operator|!
name|overriddenKeys
operator|.
name|remove
argument_list|(
name|binding
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
name|super
operator|.
name|visit
argument_list|(
name|binding
argument_list|)
expr_stmt|;
comment|// Record when a scope instance is used in a binding
name|Scope
name|scope
init|=
name|getScopeInstanceOrNull
argument_list|(
name|binding
argument_list|)
decl_stmt|;
if|if
condition|(
name|scope
operator|!=
literal|null
condition|)
block|{
name|scopeInstancesInUse
operator|.
name|put
argument_list|(
name|scope
argument_list|,
name|binding
operator|.
name|getSource
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Void
name|visit
parameter_list|(
name|PrivateElements
name|privateElements
parameter_list|)
block|{
name|PrivateBinder
name|privateBinder
init|=
name|binder
operator|.
name|withSource
argument_list|(
name|privateElements
operator|.
name|getSource
argument_list|()
argument_list|)
operator|.
name|newPrivateBinder
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Key
argument_list|<
name|?
argument_list|>
argument_list|>
name|skippedExposes
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
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
if|if
condition|(
name|overriddenKeys
operator|.
name|remove
argument_list|(
name|key
argument_list|)
condition|)
block|{
name|skippedExposes
operator|.
name|add
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|privateBinder
operator|.
name|withSource
argument_list|(
name|privateElements
operator|.
name|getExposedSource
argument_list|(
name|key
argument_list|)
argument_list|)
operator|.
name|expose
argument_list|(
name|key
argument_list|)
expr_stmt|;
block|}
block|}
comment|// we're not skipping deep exposes, but that should be okay. If we ever need to, we
comment|// have to search through this set of elements for PrivateElements, recursively
for|for
control|(
name|Element
name|element
range|:
name|privateElements
operator|.
name|getElements
argument_list|()
control|)
block|{
if|if
condition|(
name|element
operator|instanceof
name|Binding
operator|&&
name|skippedExposes
operator|.
name|contains
argument_list|(
operator|(
operator|(
name|Binding
operator|)
name|element
operator|)
operator|.
name|getKey
argument_list|()
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|element
operator|.
name|applyTo
argument_list|(
name|privateBinder
argument_list|)
expr_stmt|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Void
name|visit
parameter_list|(
name|ScopeBinding
name|scopeBinding
parameter_list|)
block|{
name|scopeBindings
operator|.
name|add
argument_list|(
name|scopeBinding
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
operator|.
name|writeAll
argument_list|(
name|elements
argument_list|)
expr_stmt|;
comment|// execute the scope bindings, skipping scopes that have been overridden. Any scope that
comment|// is overridden and in active use will prompt an error
operator|new
name|ModuleWriter
argument_list|(
name|binder
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|Void
name|visit
parameter_list|(
name|ScopeBinding
name|scopeBinding
parameter_list|)
block|{
if|if
condition|(
operator|!
name|overridesScopeAnnotations
operator|.
name|remove
argument_list|(
name|scopeBinding
operator|.
name|getAnnotationType
argument_list|()
argument_list|)
condition|)
block|{
name|super
operator|.
name|visit
argument_list|(
name|scopeBinding
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Object
name|source
init|=
name|scopeInstancesInUse
operator|.
name|get
argument_list|(
name|scopeBinding
operator|.
name|getScope
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|source
operator|!=
literal|null
condition|)
block|{
name|binder
argument_list|()
operator|.
name|withSource
argument_list|(
name|source
argument_list|)
operator|.
name|addError
argument_list|(
literal|"The scope for @%s is bound directly and cannot be overridden."
argument_list|,
name|scopeBinding
operator|.
name|getAnnotationType
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
literal|null
return|;
block|}
block|}
operator|.
name|writeAll
argument_list|(
name|scopeBindings
argument_list|)
expr_stmt|;
comment|// TODO: bind the overridden keys using multibinder
block|}
specifier|private
name|Scope
name|getScopeInstanceOrNull
parameter_list|(
name|Binding
argument_list|<
name|?
argument_list|>
name|binding
parameter_list|)
block|{
return|return
name|binding
operator|.
name|acceptScopingVisitor
argument_list|(
operator|new
name|DefaultBindingScopingVisitor
argument_list|<
name|Scope
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Scope
name|visitScope
parameter_list|(
name|Scope
name|scope
parameter_list|)
block|{
return|return
name|scope
return|;
block|}
block|}
argument_list|)
return|;
block|}
block|}
return|;
block|}
block|}
DECL|class|ModuleWriter
specifier|private
specifier|static
class|class
name|ModuleWriter
extends|extends
name|DefaultElementVisitor
argument_list|<
name|Void
argument_list|>
block|{
DECL|field|binder
specifier|protected
specifier|final
name|Binder
name|binder
decl_stmt|;
DECL|method|ModuleWriter
name|ModuleWriter
parameter_list|(
name|Binder
name|binder
parameter_list|)
block|{
name|this
operator|.
name|binder
operator|=
name|binder
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|visitOther
specifier|protected
name|Void
name|visitOther
parameter_list|(
name|Element
name|element
parameter_list|)
block|{
name|element
operator|.
name|applyTo
argument_list|(
name|binder
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
DECL|method|writeAll
name|void
name|writeAll
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
name|Element
argument_list|>
name|elements
parameter_list|)
block|{
for|for
control|(
name|Element
name|element
range|:
name|elements
control|)
block|{
name|element
operator|.
name|acceptVisitor
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

