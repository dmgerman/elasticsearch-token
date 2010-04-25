begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.guice.inject
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
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
name|util
operator|.
name|guice
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
name|util
operator|.
name|guice
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
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|internal
operator|.
name|MatcherAndConverter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|inject
operator|.
name|spi
operator|.
name|TypeListenerBinding
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|ImmutableList
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
operator|.
name|ImmutableSet
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
comment|/**  * The inheritable data within an injector. This class is intended to allow parent and local  * injector data to be accessed as a unit.  *  * @author jessewilson@google.com (Jesse Wilson)  */
end_comment

begin_interface
DECL|interface|State
interface|interface
name|State
block|{
DECL|field|NONE
specifier|static
specifier|final
name|State
name|NONE
init|=
operator|new
name|State
argument_list|()
block|{
specifier|public
name|State
name|parent
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
parameter_list|<
name|T
parameter_list|>
name|BindingImpl
argument_list|<
name|T
argument_list|>
name|getExplicitBinding
parameter_list|(
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
specifier|public
name|Map
argument_list|<
name|Key
argument_list|<
name|?
argument_list|>
argument_list|,
name|Binding
argument_list|<
name|?
argument_list|>
argument_list|>
name|getExplicitBindingsThisLevel
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|void
name|putBinding
parameter_list|(
name|Key
argument_list|<
name|?
argument_list|>
name|key
parameter_list|,
name|BindingImpl
argument_list|<
name|?
argument_list|>
name|binding
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|Scope
name|getScope
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|scopingAnnotation
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
specifier|public
name|void
name|putAnnotation
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|annotationType
parameter_list|,
name|Scope
name|scope
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|void
name|addConverter
parameter_list|(
name|MatcherAndConverter
name|matcherAndConverter
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|MatcherAndConverter
name|getConverter
parameter_list|(
name|String
name|stringValue
parameter_list|,
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|type
parameter_list|,
name|Errors
name|errors
parameter_list|,
name|Object
name|source
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|Iterable
argument_list|<
name|MatcherAndConverter
argument_list|>
name|getConvertersThisLevel
parameter_list|()
block|{
return|return
name|ImmutableSet
operator|.
name|of
argument_list|()
return|;
block|}
specifier|public
name|void
name|addTypeListener
parameter_list|(
name|TypeListenerBinding
name|typeListenerBinding
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
specifier|public
name|List
argument_list|<
name|TypeListenerBinding
argument_list|>
name|getTypeListenerBindings
parameter_list|()
block|{
return|return
name|ImmutableList
operator|.
name|of
argument_list|()
return|;
block|}
specifier|public
name|void
name|blacklist
parameter_list|(
name|Key
argument_list|<
name|?
argument_list|>
name|key
parameter_list|)
block|{     }
specifier|public
name|boolean
name|isBlacklisted
parameter_list|(
name|Key
argument_list|<
name|?
argument_list|>
name|key
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
specifier|public
name|Object
name|lock
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
decl_stmt|;
DECL|method|parent
name|State
name|parent
parameter_list|()
function_decl|;
comment|/** Gets a binding which was specified explicitly in a module, or null. */
DECL|method|getExplicitBinding
parameter_list|<
name|T
parameter_list|>
name|BindingImpl
argument_list|<
name|T
argument_list|>
name|getExplicitBinding
parameter_list|(
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|)
function_decl|;
comment|/** Returns the explicit bindings at this level only. */
DECL|method|getExplicitBindingsThisLevel
name|Map
argument_list|<
name|Key
argument_list|<
name|?
argument_list|>
argument_list|,
name|Binding
argument_list|<
name|?
argument_list|>
argument_list|>
name|getExplicitBindingsThisLevel
parameter_list|()
function_decl|;
DECL|method|putBinding
name|void
name|putBinding
parameter_list|(
name|Key
argument_list|<
name|?
argument_list|>
name|key
parameter_list|,
name|BindingImpl
argument_list|<
name|?
argument_list|>
name|binding
parameter_list|)
function_decl|;
comment|/** Returns the matching scope, or null. */
DECL|method|getScope
name|Scope
name|getScope
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|scopingAnnotation
parameter_list|)
function_decl|;
DECL|method|putAnnotation
name|void
name|putAnnotation
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Annotation
argument_list|>
name|annotationType
parameter_list|,
name|Scope
name|scope
parameter_list|)
function_decl|;
DECL|method|addConverter
name|void
name|addConverter
parameter_list|(
name|MatcherAndConverter
name|matcherAndConverter
parameter_list|)
function_decl|;
comment|/** Returns the matching converter for {@code type}, or null if none match. */
DECL|method|getConverter
name|MatcherAndConverter
name|getConverter
parameter_list|(
name|String
name|stringValue
parameter_list|,
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|type
parameter_list|,
name|Errors
name|errors
parameter_list|,
name|Object
name|source
parameter_list|)
function_decl|;
comment|/** Returns all converters at this level only. */
DECL|method|getConvertersThisLevel
name|Iterable
argument_list|<
name|MatcherAndConverter
argument_list|>
name|getConvertersThisLevel
parameter_list|()
function_decl|;
DECL|method|addTypeListener
name|void
name|addTypeListener
parameter_list|(
name|TypeListenerBinding
name|typeListenerBinding
parameter_list|)
function_decl|;
DECL|method|getTypeListenerBindings
name|List
argument_list|<
name|TypeListenerBinding
argument_list|>
name|getTypeListenerBindings
parameter_list|()
function_decl|;
comment|/**    * Forbids the corresponding injector from creating a binding to {@code key}. Child injectors    * blacklist their bound keys on their parent injectors to prevent just-in-time bindings on the    * parent injector that would conflict.    */
DECL|method|blacklist
name|void
name|blacklist
parameter_list|(
name|Key
argument_list|<
name|?
argument_list|>
name|key
parameter_list|)
function_decl|;
comment|/**    * Returns true if {@code key} is forbidden from being bound in this injector. This indicates that    * one of this injector's descendent's has bound the key.    */
DECL|method|isBlacklisted
name|boolean
name|isBlacklisted
parameter_list|(
name|Key
argument_list|<
name|?
argument_list|>
name|key
parameter_list|)
function_decl|;
comment|/**    * Returns the shared lock for all injector data. This is a low-granularity, high-contention lock    * to be used when reading mutable data (ie. just-in-time bindings, and binding blacklists).    */
DECL|method|lock
name|Object
name|lock
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

