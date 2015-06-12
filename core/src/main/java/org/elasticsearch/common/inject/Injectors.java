begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|matcher
operator|.
name|Matcher
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
name|name
operator|.
name|Names
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
name|Message
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
name|Type
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
name|Map
operator|.
name|Entry
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
comment|/**  *  */
end_comment

begin_class
DECL|class|Injectors
specifier|public
class|class
name|Injectors
block|{
DECL|method|getFirstErrorFailure
specifier|public
specifier|static
name|Throwable
name|getFirstErrorFailure
parameter_list|(
name|CreationException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|.
name|getErrorMessages
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|e
return|;
block|}
comment|// return the first message that has root cause, probably an actual error
for|for
control|(
name|Message
name|message
range|:
name|e
operator|.
name|getErrorMessages
argument_list|()
control|)
block|{
if|if
condition|(
name|message
operator|.
name|getCause
argument_list|()
operator|!=
literal|null
condition|)
block|{
return|return
name|message
operator|.
name|getCause
argument_list|()
return|;
block|}
block|}
return|return
name|e
return|;
block|}
comment|/**      * Returns an instance of the given type with the {@link org.elasticsearch.common.inject.name.Named}      * annotation value.      *<p/>      * This method allows you to switch this code      *<code>injector.getInstance(Key.get(type, Names.named(name)));</code>      *<p/>      * to the more concise      *<code>Injectors.getInstance(injector, type, name);</code>      */
DECL|method|getInstance
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|getInstance
parameter_list|(
name|Injector
name|injector
parameter_list|,
name|java
operator|.
name|lang
operator|.
name|Class
argument_list|<
name|T
argument_list|>
name|type
parameter_list|,
name|String
name|name
parameter_list|)
block|{
return|return
name|injector
operator|.
name|getInstance
argument_list|(
name|Key
operator|.
name|get
argument_list|(
name|type
argument_list|,
name|Names
operator|.
name|named
argument_list|(
name|name
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Returns a collection of all instances of the given base type      *      * @param baseClass the base type of objects required      * @param<T>       the base type      * @return a set of objects returned from this injector      */
DECL|method|getInstancesOf
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Set
argument_list|<
name|T
argument_list|>
name|getInstancesOf
parameter_list|(
name|Injector
name|injector
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|baseClass
parameter_list|)
block|{
name|Set
argument_list|<
name|T
argument_list|>
name|answer
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Entry
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
argument_list|>
name|entries
init|=
name|injector
operator|.
name|getBindings
argument_list|()
operator|.
name|entrySet
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
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
name|entry
range|:
name|entries
control|)
block|{
name|Key
argument_list|<
name|?
argument_list|>
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|keyType
init|=
name|getKeyType
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyType
operator|!=
literal|null
operator|&&
name|baseClass
operator|.
name|isAssignableFrom
argument_list|(
name|keyType
argument_list|)
condition|)
block|{
name|Binding
argument_list|<
name|?
argument_list|>
name|binding
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Object
name|value
init|=
name|binding
operator|.
name|getProvider
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
name|T
name|castValue
init|=
name|baseClass
operator|.
name|cast
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|answer
operator|.
name|add
argument_list|(
name|castValue
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|answer
return|;
block|}
comment|/**      * Returns a collection of all instances matching the given matcher      *      * @param matcher matches the types to return instances      * @return a set of objects returned from this injector      */
DECL|method|getInstancesOf
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Set
argument_list|<
name|T
argument_list|>
name|getInstancesOf
parameter_list|(
name|Injector
name|injector
parameter_list|,
name|Matcher
argument_list|<
name|Class
argument_list|>
name|matcher
parameter_list|)
block|{
name|Set
argument_list|<
name|T
argument_list|>
name|answer
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Entry
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
argument_list|>
name|entries
init|=
name|injector
operator|.
name|getBindings
argument_list|()
operator|.
name|entrySet
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
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
name|entry
range|:
name|entries
control|)
block|{
name|Key
argument_list|<
name|?
argument_list|>
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|keyType
init|=
name|getKeyType
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyType
operator|!=
literal|null
operator|&&
name|matcher
operator|.
name|matches
argument_list|(
name|keyType
argument_list|)
condition|)
block|{
name|Binding
argument_list|<
name|?
argument_list|>
name|binding
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|Object
name|value
init|=
name|binding
operator|.
name|getProvider
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|answer
operator|.
name|add
argument_list|(
operator|(
name|T
operator|)
name|value
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|answer
return|;
block|}
comment|/**      * Returns a collection of all of the providers matching the given matcher      *      * @param matcher matches the types to return instances      * @return a set of objects returned from this injector      */
DECL|method|getProvidersOf
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Set
argument_list|<
name|Provider
argument_list|<
name|T
argument_list|>
argument_list|>
name|getProvidersOf
parameter_list|(
name|Injector
name|injector
parameter_list|,
name|Matcher
argument_list|<
name|Class
argument_list|>
name|matcher
parameter_list|)
block|{
name|Set
argument_list|<
name|Provider
argument_list|<
name|T
argument_list|>
argument_list|>
name|answer
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Entry
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
argument_list|>
name|entries
init|=
name|injector
operator|.
name|getBindings
argument_list|()
operator|.
name|entrySet
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
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
name|entry
range|:
name|entries
control|)
block|{
name|Key
argument_list|<
name|?
argument_list|>
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|keyType
init|=
name|getKeyType
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyType
operator|!=
literal|null
operator|&&
name|matcher
operator|.
name|matches
argument_list|(
name|keyType
argument_list|)
condition|)
block|{
name|Binding
argument_list|<
name|?
argument_list|>
name|binding
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|answer
operator|.
name|add
argument_list|(
operator|(
name|Provider
argument_list|<
name|T
argument_list|>
operator|)
name|binding
operator|.
name|getProvider
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|answer
return|;
block|}
comment|/**      * Returns a collection of all providers of the given base type      *      * @param baseClass the base type of objects required      * @param<T>       the base type      * @return a set of objects returned from this injector      */
DECL|method|getProvidersOf
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Set
argument_list|<
name|Provider
argument_list|<
name|T
argument_list|>
argument_list|>
name|getProvidersOf
parameter_list|(
name|Injector
name|injector
parameter_list|,
name|Class
argument_list|<
name|T
argument_list|>
name|baseClass
parameter_list|)
block|{
name|Set
argument_list|<
name|Provider
argument_list|<
name|T
argument_list|>
argument_list|>
name|answer
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Entry
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
argument_list|>
name|entries
init|=
name|injector
operator|.
name|getBindings
argument_list|()
operator|.
name|entrySet
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
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
name|entry
range|:
name|entries
control|)
block|{
name|Key
argument_list|<
name|?
argument_list|>
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|keyType
init|=
name|getKeyType
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyType
operator|!=
literal|null
operator|&&
name|baseClass
operator|.
name|isAssignableFrom
argument_list|(
name|keyType
argument_list|)
condition|)
block|{
name|Binding
argument_list|<
name|?
argument_list|>
name|binding
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|answer
operator|.
name|add
argument_list|(
operator|(
name|Provider
argument_list|<
name|T
argument_list|>
operator|)
name|binding
operator|.
name|getProvider
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|answer
return|;
block|}
comment|/**      * Returns true if a binding exists for the given matcher      */
DECL|method|hasBinding
specifier|public
specifier|static
name|boolean
name|hasBinding
parameter_list|(
name|Injector
name|injector
parameter_list|,
name|Matcher
argument_list|<
name|Class
argument_list|>
name|matcher
parameter_list|)
block|{
return|return
operator|!
name|getBindingsOf
argument_list|(
name|injector
argument_list|,
name|matcher
argument_list|)
operator|.
name|isEmpty
argument_list|()
return|;
block|}
comment|/**      * Returns true if a binding exists for the given base class      */
DECL|method|hasBinding
specifier|public
specifier|static
name|boolean
name|hasBinding
parameter_list|(
name|Injector
name|injector
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|baseClass
parameter_list|)
block|{
return|return
operator|!
name|getBindingsOf
argument_list|(
name|injector
argument_list|,
name|baseClass
argument_list|)
operator|.
name|isEmpty
argument_list|()
return|;
block|}
comment|/**      * Returns true if a binding exists for the given key      */
DECL|method|hasBinding
specifier|public
specifier|static
name|boolean
name|hasBinding
parameter_list|(
name|Injector
name|injector
parameter_list|,
name|Key
argument_list|<
name|?
argument_list|>
name|key
parameter_list|)
block|{
name|Binding
argument_list|<
name|?
argument_list|>
name|binding
init|=
name|getBinding
argument_list|(
name|injector
argument_list|,
name|key
argument_list|)
decl_stmt|;
return|return
name|binding
operator|!=
literal|null
return|;
block|}
comment|/**      * Returns the binding for the given key or null if there is no such binding      */
DECL|method|getBinding
specifier|public
specifier|static
name|Binding
argument_list|<
name|?
argument_list|>
name|getBinding
parameter_list|(
name|Injector
name|injector
parameter_list|,
name|Key
argument_list|<
name|?
argument_list|>
name|key
parameter_list|)
block|{
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
name|bindings
init|=
name|injector
operator|.
name|getBindings
argument_list|()
decl_stmt|;
name|Binding
argument_list|<
name|?
argument_list|>
name|binding
init|=
name|bindings
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
return|return
name|binding
return|;
block|}
comment|/**      * Returns a collection of all of the bindings matching the given matcher      *      * @param matcher matches the types to return instances      * @return a set of objects returned from this injector      */
DECL|method|getBindingsOf
specifier|public
specifier|static
name|Set
argument_list|<
name|Binding
argument_list|<
name|?
argument_list|>
argument_list|>
name|getBindingsOf
parameter_list|(
name|Injector
name|injector
parameter_list|,
name|Matcher
argument_list|<
name|Class
argument_list|>
name|matcher
parameter_list|)
block|{
name|Set
argument_list|<
name|Binding
argument_list|<
name|?
argument_list|>
argument_list|>
name|answer
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Entry
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
argument_list|>
name|entries
init|=
name|injector
operator|.
name|getBindings
argument_list|()
operator|.
name|entrySet
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
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
name|entry
range|:
name|entries
control|)
block|{
name|Key
argument_list|<
name|?
argument_list|>
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|keyType
init|=
name|getKeyType
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyType
operator|!=
literal|null
operator|&&
name|matcher
operator|.
name|matches
argument_list|(
name|keyType
argument_list|)
condition|)
block|{
name|answer
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|answer
return|;
block|}
comment|/**      * Returns a collection of all bindings of the given base type      *      * @param baseClass the base type of objects required      * @return a set of objects returned from this injector      */
DECL|method|getBindingsOf
specifier|public
specifier|static
name|Set
argument_list|<
name|Binding
argument_list|<
name|?
argument_list|>
argument_list|>
name|getBindingsOf
parameter_list|(
name|Injector
name|injector
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|baseClass
parameter_list|)
block|{
name|Set
argument_list|<
name|Binding
argument_list|<
name|?
argument_list|>
argument_list|>
name|answer
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|Entry
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
argument_list|>
name|entries
init|=
name|injector
operator|.
name|getBindings
argument_list|()
operator|.
name|entrySet
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
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
name|entry
range|:
name|entries
control|)
block|{
name|Key
argument_list|<
name|?
argument_list|>
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|keyType
init|=
name|getKeyType
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|keyType
operator|!=
literal|null
operator|&&
name|baseClass
operator|.
name|isAssignableFrom
argument_list|(
name|keyType
argument_list|)
condition|)
block|{
name|answer
operator|.
name|add
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|answer
return|;
block|}
comment|/**      * Returns the key type of the given key      */
DECL|method|getKeyType
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Class
argument_list|<
name|?
argument_list|>
name|getKeyType
parameter_list|(
name|Key
argument_list|<
name|?
argument_list|>
name|key
parameter_list|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|keyType
init|=
literal|null
decl_stmt|;
name|TypeLiteral
argument_list|<
name|?
argument_list|>
name|typeLiteral
init|=
name|key
operator|.
name|getTypeLiteral
argument_list|()
decl_stmt|;
name|Type
name|type
init|=
name|typeLiteral
operator|.
name|getType
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|instanceof
name|Class
condition|)
block|{
name|keyType
operator|=
operator|(
name|Class
argument_list|<
name|?
argument_list|>
operator|)
name|type
expr_stmt|;
block|}
return|return
name|keyType
return|;
block|}
DECL|method|cleanCaches
specifier|public
specifier|static
name|void
name|cleanCaches
parameter_list|(
name|Injector
name|injector
parameter_list|)
block|{
operator|(
operator|(
name|InjectorImpl
operator|)
name|injector
operator|)
operator|.
name|clearCache
argument_list|()
expr_stmt|;
if|if
condition|(
name|injector
operator|.
name|getParent
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|cleanCaches
argument_list|(
name|injector
operator|.
name|getParent
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
