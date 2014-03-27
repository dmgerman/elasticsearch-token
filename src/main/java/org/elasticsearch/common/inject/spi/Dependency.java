begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright (C) 2008 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.inject.spi
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|spi
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
name|base
operator|.
name|Objects
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
name|Key
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
comment|/**  * A variable that can be resolved by an injector.  *<p/>  *<p>Use {@link #get} to build a freestanding dependency, or {@link InjectionPoint} to build one  * that's attached to a constructor, method or field.  *  * @author crazybob@google.com (Bob Lee)  * @author jessewilson@google.com (Jesse Wilson)  * @since 2.0  */
end_comment

begin_class
DECL|class|Dependency
specifier|public
specifier|final
class|class
name|Dependency
parameter_list|<
name|T
parameter_list|>
block|{
DECL|field|injectionPoint
specifier|private
specifier|final
name|InjectionPoint
name|injectionPoint
decl_stmt|;
DECL|field|key
specifier|private
specifier|final
name|Key
argument_list|<
name|T
argument_list|>
name|key
decl_stmt|;
DECL|field|nullable
specifier|private
specifier|final
name|boolean
name|nullable
decl_stmt|;
DECL|field|parameterIndex
specifier|private
specifier|final
name|int
name|parameterIndex
decl_stmt|;
DECL|method|Dependency
name|Dependency
parameter_list|(
name|InjectionPoint
name|injectionPoint
parameter_list|,
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|,
name|boolean
name|nullable
parameter_list|,
name|int
name|parameterIndex
parameter_list|)
block|{
name|this
operator|.
name|injectionPoint
operator|=
name|injectionPoint
expr_stmt|;
name|this
operator|.
name|key
operator|=
name|key
expr_stmt|;
name|this
operator|.
name|nullable
operator|=
name|nullable
expr_stmt|;
name|this
operator|.
name|parameterIndex
operator|=
name|parameterIndex
expr_stmt|;
block|}
comment|/**      * Returns a new dependency that is not attached to an injection point. The returned dependency is      * nullable.      */
DECL|method|get
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|Dependency
argument_list|<
name|T
argument_list|>
name|get
parameter_list|(
name|Key
argument_list|<
name|T
argument_list|>
name|key
parameter_list|)
block|{
return|return
operator|new
name|Dependency
argument_list|<>
argument_list|(
literal|null
argument_list|,
name|key
argument_list|,
literal|true
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
comment|/**      * Returns the dependencies from the given injection points.      */
DECL|method|forInjectionPoints
specifier|public
specifier|static
name|Set
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|forInjectionPoints
parameter_list|(
name|Set
argument_list|<
name|InjectionPoint
argument_list|>
name|injectionPoints
parameter_list|)
block|{
name|List
argument_list|<
name|Dependency
argument_list|<
name|?
argument_list|>
argument_list|>
name|dependencies
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|InjectionPoint
name|injectionPoint
range|:
name|injectionPoints
control|)
block|{
name|dependencies
operator|.
name|addAll
argument_list|(
name|injectionPoint
operator|.
name|getDependencies
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|ImmutableSet
operator|.
name|copyOf
argument_list|(
name|dependencies
argument_list|)
return|;
block|}
comment|/**      * Returns the key to the binding that satisfies this dependency.      */
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
name|this
operator|.
name|key
return|;
block|}
comment|/**      * Returns true if null is a legal value for this dependency.      */
DECL|method|isNullable
specifier|public
name|boolean
name|isNullable
parameter_list|()
block|{
return|return
name|nullable
return|;
block|}
comment|/**      * Returns the injection point to which this dependency belongs, or null if this dependency isn't      * attached to a particular injection point.      */
DECL|method|getInjectionPoint
specifier|public
name|InjectionPoint
name|getInjectionPoint
parameter_list|()
block|{
return|return
name|injectionPoint
return|;
block|}
comment|/**      * Returns the index of this dependency in the injection point's parameter list, or {@code -1} if      * this dependency does not belong to a parameter list. Only method and constuctor dependencies      * are elements in a parameter list.      */
DECL|method|getParameterIndex
specifier|public
name|int
name|getParameterIndex
parameter_list|()
block|{
return|return
name|parameterIndex
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hashCode
argument_list|(
name|injectionPoint
argument_list|,
name|parameterIndex
argument_list|,
name|key
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|instanceof
name|Dependency
condition|)
block|{
name|Dependency
name|dependency
init|=
operator|(
name|Dependency
operator|)
name|o
decl_stmt|;
return|return
name|Objects
operator|.
name|equal
argument_list|(
name|injectionPoint
argument_list|,
name|dependency
operator|.
name|injectionPoint
argument_list|)
operator|&&
name|Objects
operator|.
name|equal
argument_list|(
name|parameterIndex
argument_list|,
name|dependency
operator|.
name|parameterIndex
argument_list|)
operator|&&
name|Objects
operator|.
name|equal
argument_list|(
name|key
argument_list|,
name|dependency
operator|.
name|key
argument_list|)
return|;
block|}
else|else
block|{
return|return
literal|false
return|;
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
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|append
argument_list|(
name|key
argument_list|)
expr_stmt|;
if|if
condition|(
name|injectionPoint
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|"@"
argument_list|)
operator|.
name|append
argument_list|(
name|injectionPoint
argument_list|)
expr_stmt|;
if|if
condition|(
name|parameterIndex
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|append
argument_list|(
literal|"["
argument_list|)
operator|.
name|append
argument_list|(
name|parameterIndex
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

