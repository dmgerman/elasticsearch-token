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
name|Scopes
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
name|Singleton
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
name|Stage
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
name|binder
operator|.
name|ScopedBindingBuilder
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
name|BindingScopingVisitor
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

begin_comment
comment|/**  * References a scope, either directly (as a scope instance), or indirectly (as a scope annotation).  * The scope's eager or laziness is also exposed.  *  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_class
DECL|class|Scoping
specifier|public
specifier|abstract
class|class
name|Scoping
block|{
comment|/**      * No scoping annotation has been applied. Note that this is different from {@code      * in(Scopes.NO_SCOPE)}, where the 'NO_SCOPE' has been explicitly applied.      */
DECL|field|UNSCOPED
specifier|public
specifier|static
specifier|final
name|Scoping
name|UNSCOPED
init|=
operator|new
name|Scoping
argument_list|()
block|{
specifier|public
parameter_list|<
name|V
parameter_list|>
name|V
name|acceptVisitor
parameter_list|(
name|BindingScopingVisitor
argument_list|<
name|V
argument_list|>
name|visitor
parameter_list|)
block|{
return|return
name|visitor
operator|.
name|visitNoScoping
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Scope
name|getScopeInstance
parameter_list|()
block|{
return|return
name|Scopes
operator|.
name|NO_SCOPE
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|Scopes
operator|.
name|NO_SCOPE
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|void
name|applyTo
parameter_list|(
name|ScopedBindingBuilder
name|scopedBindingBuilder
parameter_list|)
block|{
comment|// do nothing
block|}
block|}
decl_stmt|;
DECL|field|SINGLETON_ANNOTATION
specifier|public
specifier|static
specifier|final
name|Scoping
name|SINGLETON_ANNOTATION
init|=
operator|new
name|Scoping
argument_list|()
block|{
specifier|public
parameter_list|<
name|V
parameter_list|>
name|V
name|acceptVisitor
parameter_list|(
name|BindingScopingVisitor
argument_list|<
name|V
argument_list|>
name|visitor
parameter_list|)
block|{
return|return
name|visitor
operator|.
name|visitScopeAnnotation
argument_list|(
name|Singleton
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|getScopeAnnotation
parameter_list|()
block|{
return|return
name|Singleton
operator|.
name|class
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|Singleton
operator|.
name|class
operator|.
name|getName
argument_list|()
return|;
block|}
specifier|public
name|void
name|applyTo
parameter_list|(
name|ScopedBindingBuilder
name|scopedBindingBuilder
parameter_list|)
block|{
name|scopedBindingBuilder
operator|.
name|in
argument_list|(
name|Singleton
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
DECL|field|SINGLETON_INSTANCE
specifier|public
specifier|static
specifier|final
name|Scoping
name|SINGLETON_INSTANCE
init|=
operator|new
name|Scoping
argument_list|()
block|{
specifier|public
parameter_list|<
name|V
parameter_list|>
name|V
name|acceptVisitor
parameter_list|(
name|BindingScopingVisitor
argument_list|<
name|V
argument_list|>
name|visitor
parameter_list|)
block|{
return|return
name|visitor
operator|.
name|visitScope
argument_list|(
name|Scopes
operator|.
name|SINGLETON
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Scope
name|getScopeInstance
parameter_list|()
block|{
return|return
name|Scopes
operator|.
name|SINGLETON
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|Scopes
operator|.
name|SINGLETON
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|void
name|applyTo
parameter_list|(
name|ScopedBindingBuilder
name|scopedBindingBuilder
parameter_list|)
block|{
name|scopedBindingBuilder
operator|.
name|in
argument_list|(
name|Scopes
operator|.
name|SINGLETON
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
DECL|field|EAGER_SINGLETON
specifier|public
specifier|static
specifier|final
name|Scoping
name|EAGER_SINGLETON
init|=
operator|new
name|Scoping
argument_list|()
block|{
specifier|public
parameter_list|<
name|V
parameter_list|>
name|V
name|acceptVisitor
parameter_list|(
name|BindingScopingVisitor
argument_list|<
name|V
argument_list|>
name|visitor
parameter_list|)
block|{
return|return
name|visitor
operator|.
name|visitEagerSingleton
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Scope
name|getScopeInstance
parameter_list|()
block|{
return|return
name|Scopes
operator|.
name|SINGLETON
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"eager singleton"
return|;
block|}
specifier|public
name|void
name|applyTo
parameter_list|(
name|ScopedBindingBuilder
name|scopedBindingBuilder
parameter_list|)
block|{
name|scopedBindingBuilder
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
block|}
decl_stmt|;
DECL|method|forAnnotation
specifier|public
specifier|static
name|Scoping
name|forAnnotation
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|scopingAnnotation
parameter_list|)
block|{
if|if
condition|(
name|scopingAnnotation
operator|==
name|Singleton
operator|.
name|class
condition|)
block|{
return|return
name|SINGLETON_ANNOTATION
return|;
block|}
return|return
operator|new
name|Scoping
argument_list|()
block|{
specifier|public
parameter_list|<
name|V
parameter_list|>
name|V
name|acceptVisitor
parameter_list|(
name|BindingScopingVisitor
argument_list|<
name|V
argument_list|>
name|visitor
parameter_list|)
block|{
return|return
name|visitor
operator|.
name|visitScopeAnnotation
argument_list|(
name|scopingAnnotation
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|getScopeAnnotation
parameter_list|()
block|{
return|return
name|scopingAnnotation
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|scopingAnnotation
operator|.
name|getName
argument_list|()
return|;
block|}
specifier|public
name|void
name|applyTo
parameter_list|(
name|ScopedBindingBuilder
name|scopedBindingBuilder
parameter_list|)
block|{
name|scopedBindingBuilder
operator|.
name|in
argument_list|(
name|scopingAnnotation
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
DECL|method|forInstance
specifier|public
specifier|static
name|Scoping
name|forInstance
parameter_list|(
specifier|final
name|Scope
name|scope
parameter_list|)
block|{
if|if
condition|(
name|scope
operator|==
name|Scopes
operator|.
name|SINGLETON
condition|)
block|{
return|return
name|SINGLETON_INSTANCE
return|;
block|}
return|return
operator|new
name|Scoping
argument_list|()
block|{
specifier|public
parameter_list|<
name|V
parameter_list|>
name|V
name|acceptVisitor
parameter_list|(
name|BindingScopingVisitor
argument_list|<
name|V
argument_list|>
name|visitor
parameter_list|)
block|{
return|return
name|visitor
operator|.
name|visitScope
argument_list|(
name|scope
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Scope
name|getScopeInstance
parameter_list|()
block|{
return|return
name|scope
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|scope
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|void
name|applyTo
parameter_list|(
name|ScopedBindingBuilder
name|scopedBindingBuilder
parameter_list|)
block|{
name|scopedBindingBuilder
operator|.
name|in
argument_list|(
name|scope
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
comment|/**      * Returns true if this scope was explicitly applied. If no scope was explicitly applied then the      * scoping annotation will be used.      */
DECL|method|isExplicitlyScoped
specifier|public
name|boolean
name|isExplicitlyScoped
parameter_list|()
block|{
return|return
name|this
operator|!=
name|UNSCOPED
return|;
block|}
comment|/**      * Returns true if this is the default scope. In this case a new instance will be provided for      * each injection.      */
DECL|method|isNoScope
specifier|public
name|boolean
name|isNoScope
parameter_list|()
block|{
return|return
name|getScopeInstance
argument_list|()
operator|==
name|Scopes
operator|.
name|NO_SCOPE
return|;
block|}
comment|/**      * Returns true if this scope is a singleton that should be loaded eagerly in {@code stage}.      */
DECL|method|isEagerSingleton
specifier|public
name|boolean
name|isEagerSingleton
parameter_list|(
name|Stage
name|stage
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|EAGER_SINGLETON
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|stage
operator|==
name|Stage
operator|.
name|PRODUCTION
condition|)
block|{
return|return
name|this
operator|==
name|SINGLETON_ANNOTATION
operator|||
name|this
operator|==
name|SINGLETON_INSTANCE
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**      * Returns the scope instance, or {@code null} if that isn't known for this instance.      */
DECL|method|getScopeInstance
specifier|public
name|Scope
name|getScopeInstance
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|/**      * Returns the scope annotation, or {@code null} if that isn't known for this instance.      */
DECL|method|getScopeAnnotation
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|getScopeAnnotation
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
DECL|method|acceptVisitor
specifier|public
specifier|abstract
parameter_list|<
name|V
parameter_list|>
name|V
name|acceptVisitor
parameter_list|(
name|BindingScopingVisitor
argument_list|<
name|V
argument_list|>
name|visitor
parameter_list|)
function_decl|;
DECL|method|applyTo
specifier|public
specifier|abstract
name|void
name|applyTo
parameter_list|(
name|ScopedBindingBuilder
name|scopedBindingBuilder
parameter_list|)
function_decl|;
DECL|method|Scoping
specifier|private
name|Scoping
parameter_list|()
block|{     }
block|}
end_class

end_unit

