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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gcommon
operator|.
name|base
operator|.
name|Preconditions
operator|.
name|checkNotNull
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
name|Collections
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

begin_import
import|import
name|javax
operator|.
name|annotation
operator|.
name|Nullable
import|;
end_import

begin_comment
comment|/** An ordering for a pre-existing {@code comparator}. */
end_comment

begin_class
annotation|@
name|GwtCompatible
argument_list|(
name|serializable
operator|=
literal|true
argument_list|)
DECL|class|ComparatorOrdering
specifier|final
class|class
name|ComparatorOrdering
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
DECL|field|comparator
specifier|final
name|Comparator
argument_list|<
name|T
argument_list|>
name|comparator
decl_stmt|;
DECL|method|ComparatorOrdering
name|ComparatorOrdering
parameter_list|(
name|Comparator
argument_list|<
name|T
argument_list|>
name|comparator
parameter_list|)
block|{
name|this
operator|.
name|comparator
operator|=
name|checkNotNull
argument_list|(
name|comparator
argument_list|)
expr_stmt|;
block|}
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|T
name|a
parameter_list|,
name|T
name|b
parameter_list|)
block|{
return|return
name|comparator
operator|.
name|compare
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
return|;
block|}
comment|// Override just to remove a level of indirection from inner loops
DECL|method|binarySearch
annotation|@
name|Override
specifier|public
name|int
name|binarySearch
parameter_list|(
name|List
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|sortedList
parameter_list|,
name|T
name|key
parameter_list|)
block|{
return|return
name|Collections
operator|.
name|binarySearch
argument_list|(
name|sortedList
argument_list|,
name|key
argument_list|,
name|comparator
argument_list|)
return|;
block|}
comment|// Override just to remove a level of indirection from inner loops
DECL|method|sortedCopy
annotation|@
name|Override
specifier|public
parameter_list|<
name|E
extends|extends
name|T
parameter_list|>
name|List
argument_list|<
name|E
argument_list|>
name|sortedCopy
parameter_list|(
name|Iterable
argument_list|<
name|E
argument_list|>
name|iterable
parameter_list|)
block|{
name|List
argument_list|<
name|E
argument_list|>
name|list
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|iterable
argument_list|)
decl_stmt|;
name|Collections
operator|.
name|sort
argument_list|(
name|list
argument_list|,
name|comparator
argument_list|)
expr_stmt|;
return|return
name|list
return|;
block|}
DECL|method|equals
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
annotation|@
name|Nullable
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
name|ComparatorOrdering
condition|)
block|{
name|ComparatorOrdering
argument_list|<
name|?
argument_list|>
name|that
init|=
operator|(
name|ComparatorOrdering
argument_list|<
name|?
argument_list|>
operator|)
name|object
decl_stmt|;
return|return
name|this
operator|.
name|comparator
operator|.
name|equals
argument_list|(
name|that
operator|.
name|comparator
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
name|comparator
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
name|comparator
operator|.
name|toString
argument_list|()
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

