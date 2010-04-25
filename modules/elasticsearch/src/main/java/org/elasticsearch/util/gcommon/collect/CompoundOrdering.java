begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2007 Google Inc.  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.gcommon.collect
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|collect
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
name|gcommon
operator|.
name|annotations
operator|.
name|GwtCompatible
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
comment|/** An ordering that tries several comparators in order. */
end_comment

begin_class
annotation|@
name|GwtCompatible
argument_list|(
name|serializable
operator|=
literal|true
argument_list|)
DECL|class|CompoundOrdering
specifier|final
class|class
name|CompoundOrdering
parameter_list|<
name|T
parameter_list|>
extends|extends
name|Ordering
argument_list|<
name|T
argument_list|>
implements|implements
name|Serializable
block|{
DECL|field|comparators
specifier|final
name|ImmutableList
argument_list|<
name|Comparator
argument_list|<
name|?
super|super
name|T
argument_list|>
argument_list|>
name|comparators
decl_stmt|;
DECL|method|CompoundOrdering
name|CompoundOrdering
parameter_list|(
name|Comparator
argument_list|<
name|?
super|super
name|T
argument_list|>
name|primary
parameter_list|,
name|Comparator
argument_list|<
name|?
super|super
name|T
argument_list|>
name|secondary
parameter_list|)
block|{
name|this
operator|.
name|comparators
operator|=
name|ImmutableList
operator|.
expr|<
name|Comparator
argument_list|<
name|?
super|super
name|T
argument_list|>
operator|>
name|of
argument_list|(
name|primary
argument_list|,
name|secondary
argument_list|)
expr_stmt|;
block|}
DECL|method|CompoundOrdering
name|CompoundOrdering
parameter_list|(
name|Iterable
argument_list|<
name|?
extends|extends
name|Comparator
argument_list|<
name|?
super|super
name|T
argument_list|>
argument_list|>
name|comparators
parameter_list|)
block|{
name|this
operator|.
name|comparators
operator|=
name|ImmutableList
operator|.
name|copyOf
argument_list|(
name|comparators
argument_list|)
expr_stmt|;
block|}
DECL|method|CompoundOrdering
name|CompoundOrdering
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|Comparator
argument_list|<
name|?
super|super
name|T
argument_list|>
argument_list|>
name|comparators
parameter_list|,
name|Comparator
argument_list|<
name|?
super|super
name|T
argument_list|>
name|lastComparator
parameter_list|)
block|{
name|this
operator|.
name|comparators
operator|=
operator|new
name|ImmutableList
operator|.
name|Builder
argument_list|<
name|Comparator
argument_list|<
name|?
super|super
name|T
argument_list|>
argument_list|>
argument_list|()
operator|.
name|addAll
argument_list|(
name|comparators
argument_list|)
operator|.
name|add
argument_list|(
name|lastComparator
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|T
name|left
parameter_list|,
name|T
name|right
parameter_list|)
block|{
for|for
control|(
name|Comparator
argument_list|<
name|?
super|super
name|T
argument_list|>
name|comparator
range|:
name|comparators
control|)
block|{
name|int
name|result
init|=
name|comparator
operator|.
name|compare
argument_list|(
name|left
argument_list|,
name|right
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
block|}
return|return
literal|0
return|;
block|}
DECL|method|equals
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|object
parameter_list|)
block|{
if|if
condition|(
name|object
operator|==
name|this
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|object
operator|instanceof
name|CompoundOrdering
condition|)
block|{
name|CompoundOrdering
argument_list|<
name|?
argument_list|>
name|that
init|=
operator|(
name|CompoundOrdering
argument_list|<
name|?
argument_list|>
operator|)
name|object
decl_stmt|;
return|return
name|this
operator|.
name|comparators
operator|.
name|equals
argument_list|(
name|that
operator|.
name|comparators
argument_list|)
return|;
block|}
return|return
literal|false
return|;
block|}
DECL|method|hashCode
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|comparators
operator|.
name|hashCode
argument_list|()
return|;
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Ordering.compound("
operator|+
name|comparators
operator|+
literal|")"
return|;
block|}
DECL|field|serialVersionUID
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
literal|0
decl_stmt|;
block|}
end_class

end_unit

