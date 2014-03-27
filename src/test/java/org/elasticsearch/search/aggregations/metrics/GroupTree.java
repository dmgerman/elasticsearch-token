begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
package|;
end_package

begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

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
name|Preconditions
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
name|AbstractIterator
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
name|java
operator|.
name|util
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_comment
comment|/**  * Upstream: Stream-lib, master @ 704002a2d8fa01fa7e9868dae9d0c8bedd8e9427  * https://github.com/addthis/stream-lib/blob/master/src/main/java/com/clearspring/analytics/stream/quantile/GroupTree.java  */
end_comment

begin_comment
comment|/**  * A tree containing TDigest.Group.  This adds to the normal NavigableSet the  * ability to sum up the size of elements to the left of a particular group.  */
end_comment

begin_class
DECL|class|GroupTree
specifier|public
class|class
name|GroupTree
implements|implements
name|Iterable
argument_list|<
name|GroupTree
operator|.
name|Group
argument_list|>
block|{
DECL|field|count
specifier|private
name|int
name|count
decl_stmt|;
DECL|field|size
name|int
name|size
decl_stmt|;
DECL|field|depth
specifier|private
name|int
name|depth
decl_stmt|;
DECL|field|leaf
specifier|private
name|Group
name|leaf
decl_stmt|;
DECL|field|left
DECL|field|right
specifier|private
name|GroupTree
name|left
decl_stmt|,
name|right
decl_stmt|;
DECL|method|GroupTree
specifier|public
name|GroupTree
parameter_list|()
block|{
name|count
operator|=
name|size
operator|=
name|depth
operator|=
literal|0
expr_stmt|;
name|leaf
operator|=
literal|null
expr_stmt|;
name|left
operator|=
name|right
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|GroupTree
specifier|public
name|GroupTree
parameter_list|(
name|Group
name|leaf
parameter_list|)
block|{
name|size
operator|=
name|depth
operator|=
literal|1
expr_stmt|;
name|this
operator|.
name|leaf
operator|=
name|leaf
expr_stmt|;
name|count
operator|=
name|leaf
operator|.
name|count
argument_list|()
expr_stmt|;
name|left
operator|=
name|right
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|GroupTree
specifier|public
name|GroupTree
parameter_list|(
name|GroupTree
name|left
parameter_list|,
name|GroupTree
name|right
parameter_list|)
block|{
name|this
operator|.
name|left
operator|=
name|left
expr_stmt|;
name|this
operator|.
name|right
operator|=
name|right
expr_stmt|;
name|count
operator|=
name|left
operator|.
name|count
operator|+
name|right
operator|.
name|count
expr_stmt|;
name|size
operator|=
name|left
operator|.
name|size
operator|+
name|right
operator|.
name|size
expr_stmt|;
name|rebalance
argument_list|()
expr_stmt|;
name|leaf
operator|=
name|this
operator|.
name|right
operator|.
name|first
argument_list|()
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|Group
name|group
parameter_list|)
block|{
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
name|leaf
operator|=
name|group
expr_stmt|;
name|depth
operator|=
literal|1
expr_stmt|;
name|count
operator|=
name|group
operator|.
name|count
argument_list|()
expr_stmt|;
name|size
operator|=
literal|1
expr_stmt|;
return|return;
block|}
elseif|else
if|if
condition|(
name|size
operator|==
literal|1
condition|)
block|{
name|int
name|order
init|=
name|group
operator|.
name|compareTo
argument_list|(
name|leaf
argument_list|)
decl_stmt|;
if|if
condition|(
name|order
operator|<
literal|0
condition|)
block|{
name|left
operator|=
operator|new
name|GroupTree
argument_list|(
name|group
argument_list|)
expr_stmt|;
name|right
operator|=
operator|new
name|GroupTree
argument_list|(
name|leaf
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|order
operator|>
literal|0
condition|)
block|{
name|left
operator|=
operator|new
name|GroupTree
argument_list|(
name|leaf
argument_list|)
expr_stmt|;
name|right
operator|=
operator|new
name|GroupTree
argument_list|(
name|group
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|group
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|group
operator|.
name|compareTo
argument_list|(
name|leaf
argument_list|)
operator|<
literal|0
condition|)
block|{
name|left
operator|.
name|add
argument_list|(
name|group
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|right
operator|.
name|add
argument_list|(
name|group
argument_list|)
expr_stmt|;
block|}
name|count
operator|+=
name|group
operator|.
name|count
argument_list|()
expr_stmt|;
name|size
operator|++
expr_stmt|;
name|depth
operator|=
name|Math
operator|.
name|max
argument_list|(
name|left
operator|.
name|depth
argument_list|,
name|right
operator|.
name|depth
argument_list|)
operator|+
literal|1
expr_stmt|;
name|rebalance
argument_list|()
expr_stmt|;
block|}
DECL|method|rebalance
specifier|private
name|void
name|rebalance
parameter_list|()
block|{
name|int
name|l
init|=
name|left
operator|.
name|depth
argument_list|()
decl_stmt|;
name|int
name|r
init|=
name|right
operator|.
name|depth
argument_list|()
decl_stmt|;
if|if
condition|(
name|l
operator|>
name|r
operator|+
literal|1
condition|)
block|{
if|if
condition|(
name|left
operator|.
name|left
operator|.
name|depth
argument_list|()
operator|>
name|left
operator|.
name|right
operator|.
name|depth
argument_list|()
condition|)
block|{
name|rotate
argument_list|(
name|left
operator|.
name|left
operator|.
name|left
argument_list|,
name|left
operator|.
name|left
operator|.
name|right
argument_list|,
name|left
operator|.
name|right
argument_list|,
name|right
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rotate
argument_list|(
name|left
operator|.
name|left
argument_list|,
name|left
operator|.
name|right
operator|.
name|left
argument_list|,
name|left
operator|.
name|right
operator|.
name|right
argument_list|,
name|right
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|r
operator|>
name|l
operator|+
literal|1
condition|)
block|{
if|if
condition|(
name|right
operator|.
name|left
operator|.
name|depth
argument_list|()
operator|>
name|right
operator|.
name|right
operator|.
name|depth
argument_list|()
condition|)
block|{
name|rotate
argument_list|(
name|left
argument_list|,
name|right
operator|.
name|left
operator|.
name|left
argument_list|,
name|right
operator|.
name|left
operator|.
name|right
argument_list|,
name|right
operator|.
name|right
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rotate
argument_list|(
name|left
argument_list|,
name|right
operator|.
name|left
argument_list|,
name|right
operator|.
name|right
operator|.
name|left
argument_list|,
name|right
operator|.
name|right
operator|.
name|right
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|depth
operator|=
name|Math
operator|.
name|max
argument_list|(
name|left
operator|.
name|depth
argument_list|()
argument_list|,
name|right
operator|.
name|depth
argument_list|()
argument_list|)
operator|+
literal|1
expr_stmt|;
block|}
block|}
DECL|method|rotate
specifier|private
name|void
name|rotate
parameter_list|(
name|GroupTree
name|a
parameter_list|,
name|GroupTree
name|b
parameter_list|,
name|GroupTree
name|c
parameter_list|,
name|GroupTree
name|d
parameter_list|)
block|{
name|left
operator|=
operator|new
name|GroupTree
argument_list|(
name|a
argument_list|,
name|b
argument_list|)
expr_stmt|;
name|right
operator|=
operator|new
name|GroupTree
argument_list|(
name|c
argument_list|,
name|d
argument_list|)
expr_stmt|;
name|count
operator|=
name|left
operator|.
name|count
operator|+
name|right
operator|.
name|count
expr_stmt|;
name|size
operator|=
name|left
operator|.
name|size
operator|+
name|right
operator|.
name|size
expr_stmt|;
name|depth
operator|=
name|Math
operator|.
name|max
argument_list|(
name|left
operator|.
name|depth
argument_list|()
argument_list|,
name|right
operator|.
name|depth
argument_list|()
argument_list|)
operator|+
literal|1
expr_stmt|;
name|leaf
operator|=
name|right
operator|.
name|first
argument_list|()
expr_stmt|;
block|}
DECL|method|depth
specifier|private
name|int
name|depth
parameter_list|()
block|{
return|return
name|depth
return|;
block|}
DECL|method|size
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|size
return|;
block|}
comment|/**      * @return the number of items strictly before the current element      */
DECL|method|headCount
specifier|public
name|int
name|headCount
parameter_list|(
name|Group
name|base
parameter_list|)
block|{
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|left
operator|==
literal|null
condition|)
block|{
return|return
name|leaf
operator|.
name|compareTo
argument_list|(
name|base
argument_list|)
operator|<
literal|0
condition|?
literal|1
else|:
literal|0
return|;
block|}
else|else
block|{
if|if
condition|(
name|base
operator|.
name|compareTo
argument_list|(
name|leaf
argument_list|)
operator|<
literal|0
condition|)
block|{
return|return
name|left
operator|.
name|headCount
argument_list|(
name|base
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|left
operator|.
name|size
operator|+
name|right
operator|.
name|headCount
argument_list|(
name|base
argument_list|)
return|;
block|}
block|}
block|}
comment|/**      * @return the sum of the size() function for all elements strictly before the current element.      */
DECL|method|headSum
specifier|public
name|int
name|headSum
parameter_list|(
name|Group
name|base
parameter_list|)
block|{
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
return|return
literal|0
return|;
block|}
elseif|else
if|if
condition|(
name|left
operator|==
literal|null
condition|)
block|{
return|return
name|leaf
operator|.
name|compareTo
argument_list|(
name|base
argument_list|)
operator|<
literal|0
condition|?
name|count
else|:
literal|0
return|;
block|}
else|else
block|{
if|if
condition|(
name|base
operator|.
name|compareTo
argument_list|(
name|leaf
argument_list|)
operator|<=
literal|0
condition|)
block|{
return|return
name|left
operator|.
name|headSum
argument_list|(
name|base
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|left
operator|.
name|count
operator|+
name|right
operator|.
name|headSum
argument_list|(
name|base
argument_list|)
return|;
block|}
block|}
block|}
comment|/**      * @return the first Group in this set      */
DECL|method|first
specifier|public
name|Group
name|first
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|size
operator|>
literal|0
argument_list|,
literal|"No first element of empty set"
argument_list|)
expr_stmt|;
if|if
condition|(
name|left
operator|==
literal|null
condition|)
block|{
return|return
name|leaf
return|;
block|}
else|else
block|{
return|return
name|left
operator|.
name|first
argument_list|()
return|;
block|}
block|}
comment|/**      * Iteratres through all groups in the tree.      */
DECL|method|iterator
specifier|public
name|Iterator
argument_list|<
name|Group
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|iterator
argument_list|(
literal|null
argument_list|)
return|;
block|}
comment|/**      * Iterates through all of the Groups in this tree in ascending order of means      *      * @param start The place to start this subset.  Remember that Groups are ordered by mean *and* id.      * @return An iterator that goes through the groups in order of mean and id starting at or after the      *         specified Group.      */
DECL|method|iterator
specifier|private
name|Iterator
argument_list|<
name|Group
argument_list|>
name|iterator
parameter_list|(
specifier|final
name|Group
name|start
parameter_list|)
block|{
return|return
operator|new
name|AbstractIterator
argument_list|<
name|Group
argument_list|>
argument_list|()
block|{
block|{
name|stack
operator|=
operator|new
name|ArrayDeque
argument_list|<>
argument_list|()
expr_stmt|;
name|push
parameter_list|(
name|GroupTree
operator|.
name|this
parameter_list|,
name|start
parameter_list|)
constructor_decl|;
block|}
name|Deque
argument_list|<
name|GroupTree
argument_list|>
name|stack
decl_stmt|;
comment|// recurses down to the leaf that is>= start
comment|// pending right hand branches on the way are put on the stack
specifier|private
name|void
name|push
parameter_list|(
name|GroupTree
name|z
parameter_list|,
name|Group
name|start
parameter_list|)
block|{
while|while
condition|(
name|z
operator|.
name|left
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|start
operator|==
literal|null
operator|||
name|start
operator|.
name|compareTo
argument_list|(
name|z
operator|.
name|leaf
argument_list|)
operator|<
literal|0
condition|)
block|{
comment|// remember we will have to process the right hand branch later
name|stack
operator|.
name|push
argument_list|(
name|z
operator|.
name|right
argument_list|)
expr_stmt|;
comment|// note that there is no guarantee that z.left has any good data
name|z
operator|=
name|z
operator|.
name|left
expr_stmt|;
block|}
else|else
block|{
comment|// if the left hand branch doesn't contain start, then no push
name|z
operator|=
name|z
operator|.
name|right
expr_stmt|;
block|}
block|}
comment|// put the leaf value on the stack if it is valid
if|if
condition|(
name|start
operator|==
literal|null
operator|||
name|z
operator|.
name|leaf
operator|.
name|compareTo
argument_list|(
name|start
argument_list|)
operator|>=
literal|0
condition|)
block|{
name|stack
operator|.
name|push
argument_list|(
name|z
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|Group
name|computeNext
parameter_list|()
block|{
name|GroupTree
name|r
init|=
name|stack
operator|.
name|poll
argument_list|()
decl_stmt|;
while|while
condition|(
name|r
operator|!=
literal|null
operator|&&
name|r
operator|.
name|left
operator|!=
literal|null
condition|)
block|{
comment|// unpack r onto the stack
name|push
argument_list|(
name|r
argument_list|,
name|start
argument_list|)
expr_stmt|;
name|r
operator|=
name|stack
operator|.
name|poll
argument_list|()
expr_stmt|;
block|}
comment|// at this point, r == null or r.left == null
comment|// if r == null, stack is empty and we are done
comment|// if r != null, then r.left != null and we have a result
if|if
condition|(
name|r
operator|!=
literal|null
condition|)
block|{
return|return
name|r
operator|.
name|leaf
return|;
block|}
return|return
name|endOfData
argument_list|()
return|;
block|}
block|}
return|;
block|}
DECL|method|remove
specifier|public
name|void
name|remove
parameter_list|(
name|Group
name|base
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|size
operator|>
literal|0
argument_list|,
literal|"Cannot remove from empty set"
argument_list|)
expr_stmt|;
if|if
condition|(
name|size
operator|==
literal|1
condition|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|base
operator|.
name|compareTo
argument_list|(
name|leaf
argument_list|)
operator|==
literal|0
argument_list|,
literal|"Element %s not found"
argument_list|,
name|base
argument_list|)
expr_stmt|;
name|count
operator|=
name|size
operator|=
literal|0
expr_stmt|;
name|leaf
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|base
operator|.
name|compareTo
argument_list|(
name|leaf
argument_list|)
operator|<
literal|0
condition|)
block|{
if|if
condition|(
name|left
operator|.
name|size
operator|>
literal|1
condition|)
block|{
name|left
operator|.
name|remove
argument_list|(
name|base
argument_list|)
expr_stmt|;
name|count
operator|-=
name|base
operator|.
name|count
argument_list|()
expr_stmt|;
name|size
operator|--
expr_stmt|;
name|rebalance
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|size
operator|=
name|right
operator|.
name|size
expr_stmt|;
name|count
operator|=
name|right
operator|.
name|count
expr_stmt|;
name|depth
operator|=
name|right
operator|.
name|depth
expr_stmt|;
name|leaf
operator|=
name|right
operator|.
name|leaf
expr_stmt|;
name|left
operator|=
name|right
operator|.
name|left
expr_stmt|;
name|right
operator|=
name|right
operator|.
name|right
expr_stmt|;
block|}
block|}
else|else
block|{
if|if
condition|(
name|right
operator|.
name|size
operator|>
literal|1
condition|)
block|{
name|right
operator|.
name|remove
argument_list|(
name|base
argument_list|)
expr_stmt|;
name|leaf
operator|=
name|right
operator|.
name|first
argument_list|()
expr_stmt|;
name|count
operator|-=
name|base
operator|.
name|count
argument_list|()
expr_stmt|;
name|size
operator|--
expr_stmt|;
name|rebalance
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|size
operator|=
name|left
operator|.
name|size
expr_stmt|;
name|count
operator|=
name|left
operator|.
name|count
expr_stmt|;
name|depth
operator|=
name|left
operator|.
name|depth
expr_stmt|;
name|leaf
operator|=
name|left
operator|.
name|leaf
expr_stmt|;
name|right
operator|=
name|left
operator|.
name|right
expr_stmt|;
name|left
operator|=
name|left
operator|.
name|left
expr_stmt|;
block|}
block|}
block|}
block|}
comment|/**      * @return the largest element less than or equal to base      */
DECL|method|floor
specifier|public
name|Group
name|floor
parameter_list|(
name|Group
name|base
parameter_list|)
block|{
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
else|else
block|{
if|if
condition|(
name|size
operator|==
literal|1
condition|)
block|{
return|return
name|base
operator|.
name|compareTo
argument_list|(
name|leaf
argument_list|)
operator|>=
literal|0
condition|?
name|leaf
else|:
literal|null
return|;
block|}
else|else
block|{
if|if
condition|(
name|base
operator|.
name|compareTo
argument_list|(
name|leaf
argument_list|)
operator|<
literal|0
condition|)
block|{
return|return
name|left
operator|.
name|floor
argument_list|(
name|base
argument_list|)
return|;
block|}
else|else
block|{
name|Group
name|floor
init|=
name|right
operator|.
name|floor
argument_list|(
name|base
argument_list|)
decl_stmt|;
if|if
condition|(
name|floor
operator|==
literal|null
condition|)
block|{
name|floor
operator|=
name|left
operator|.
name|last
argument_list|()
expr_stmt|;
block|}
return|return
name|floor
return|;
block|}
block|}
block|}
block|}
DECL|method|last
specifier|public
name|Group
name|last
parameter_list|()
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|size
operator|>
literal|0
argument_list|,
literal|"Cannot find last element of empty set"
argument_list|)
expr_stmt|;
if|if
condition|(
name|size
operator|==
literal|1
condition|)
block|{
return|return
name|leaf
return|;
block|}
else|else
block|{
return|return
name|right
operator|.
name|last
argument_list|()
return|;
block|}
block|}
comment|/**      * @return the smallest element greater than or equal to base.      */
DECL|method|ceiling
specifier|public
name|Group
name|ceiling
parameter_list|(
name|Group
name|base
parameter_list|)
block|{
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|size
operator|==
literal|1
condition|)
block|{
return|return
name|base
operator|.
name|compareTo
argument_list|(
name|leaf
argument_list|)
operator|<=
literal|0
condition|?
name|leaf
else|:
literal|null
return|;
block|}
else|else
block|{
if|if
condition|(
name|base
operator|.
name|compareTo
argument_list|(
name|leaf
argument_list|)
operator|<
literal|0
condition|)
block|{
name|Group
name|r
init|=
name|left
operator|.
name|ceiling
argument_list|(
name|base
argument_list|)
decl_stmt|;
if|if
condition|(
name|r
operator|==
literal|null
condition|)
block|{
name|r
operator|=
name|right
operator|.
name|first
argument_list|()
expr_stmt|;
block|}
return|return
name|r
return|;
block|}
else|else
block|{
return|return
name|right
operator|.
name|ceiling
argument_list|(
name|base
argument_list|)
return|;
block|}
block|}
block|}
comment|/**      * @return the subset of elements equal to or greater than base.      */
DECL|method|tailSet
specifier|public
name|Iterable
argument_list|<
name|Group
argument_list|>
name|tailSet
parameter_list|(
specifier|final
name|Group
name|start
parameter_list|)
block|{
return|return
operator|new
name|Iterable
argument_list|<
name|Group
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Iterator
argument_list|<
name|Group
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|GroupTree
operator|.
name|this
operator|.
name|iterator
argument_list|(
name|start
argument_list|)
return|;
block|}
block|}
return|;
block|}
DECL|method|sum
specifier|public
name|int
name|sum
parameter_list|()
block|{
return|return
name|count
return|;
block|}
DECL|method|checkBalance
specifier|public
name|void
name|checkBalance
parameter_list|()
block|{
if|if
condition|(
name|left
operator|!=
literal|null
condition|)
block|{
name|Preconditions
operator|.
name|checkState
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
name|left
operator|.
name|depth
argument_list|()
operator|-
name|right
operator|.
name|depth
argument_list|()
argument_list|)
operator|<
literal|2
argument_list|,
literal|"Imbalanced"
argument_list|)
expr_stmt|;
name|int
name|l
init|=
name|left
operator|.
name|depth
argument_list|()
decl_stmt|;
name|int
name|r
init|=
name|right
operator|.
name|depth
argument_list|()
decl_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|depth
operator|==
name|Math
operator|.
name|max
argument_list|(
name|l
argument_list|,
name|r
argument_list|)
operator|+
literal|1
argument_list|,
literal|"Depth doesn't match children"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|size
operator|==
name|left
operator|.
name|size
operator|+
name|right
operator|.
name|size
argument_list|,
literal|"Sizes don't match children"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|count
operator|==
name|left
operator|.
name|count
operator|+
name|right
operator|.
name|count
argument_list|,
literal|"Counts don't match children"
argument_list|)
expr_stmt|;
name|Preconditions
operator|.
name|checkState
argument_list|(
name|leaf
operator|.
name|compareTo
argument_list|(
name|right
operator|.
name|first
argument_list|()
argument_list|)
operator|==
literal|0
argument_list|,
literal|"Split is wrong %.5d != %.5d or %d != %d"
argument_list|,
name|leaf
operator|.
name|mean
argument_list|()
argument_list|,
name|right
operator|.
name|first
argument_list|()
operator|.
name|mean
argument_list|()
argument_list|,
name|leaf
operator|.
name|id
argument_list|()
argument_list|,
name|right
operator|.
name|first
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
expr_stmt|;
name|left
operator|.
name|checkBalance
argument_list|()
expr_stmt|;
name|right
operator|.
name|checkBalance
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|print
specifier|public
name|void
name|print
parameter_list|(
name|int
name|depth
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|depth
condition|;
name|i
operator|++
control|)
block|{
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"| "
argument_list|)
expr_stmt|;
block|}
name|int
name|imbalance
init|=
name|Math
operator|.
name|abs
argument_list|(
operator|(
name|left
operator|!=
literal|null
condition|?
name|left
operator|.
name|depth
else|:
literal|1
operator|)
operator|-
operator|(
name|right
operator|!=
literal|null
condition|?
name|right
operator|.
name|depth
else|:
literal|1
operator|)
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|printf
argument_list|(
name|Locale
operator|.
name|ENGLISH
argument_list|,
literal|"%s%s, %d, %d, %d\n"
argument_list|,
operator|(
name|imbalance
operator|>
literal|1
condition|?
literal|"* "
else|:
literal|""
operator|)
operator|+
operator|(
name|right
operator|!=
literal|null
operator|&&
name|leaf
operator|.
name|compareTo
argument_list|(
name|right
operator|.
name|first
argument_list|()
argument_list|)
operator|!=
literal|0
condition|?
literal|"+ "
else|:
literal|""
operator|)
argument_list|,
name|leaf
argument_list|,
name|size
argument_list|,
name|count
argument_list|,
name|this
operator|.
name|depth
argument_list|)
expr_stmt|;
if|if
condition|(
name|left
operator|!=
literal|null
condition|)
block|{
name|left
operator|.
name|print
argument_list|(
name|depth
operator|+
literal|1
argument_list|)
expr_stmt|;
name|right
operator|.
name|print
argument_list|(
name|depth
operator|+
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|Group
specifier|public
specifier|static
class|class
name|Group
implements|implements
name|Comparable
argument_list|<
name|Group
argument_list|>
block|{
DECL|field|uniqueCount
specifier|private
specifier|static
specifier|final
name|AtomicInteger
name|uniqueCount
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|1
argument_list|)
decl_stmt|;
DECL|field|centroid
name|double
name|centroid
init|=
literal|0
decl_stmt|;
DECL|field|count
name|int
name|count
init|=
literal|0
decl_stmt|;
DECL|field|id
specifier|private
name|int
name|id
decl_stmt|;
DECL|field|actualData
specifier|private
name|List
argument_list|<
name|Double
argument_list|>
name|actualData
init|=
literal|null
decl_stmt|;
DECL|method|Group
specifier|private
name|Group
parameter_list|(
name|boolean
name|record
parameter_list|)
block|{
name|id
operator|=
name|uniqueCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
if|if
condition|(
name|record
condition|)
block|{
name|actualData
operator|=
name|Lists
operator|.
name|newArrayList
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|Group
specifier|public
name|Group
parameter_list|(
name|double
name|x
parameter_list|)
block|{
name|this
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|start
argument_list|(
name|x
argument_list|,
name|uniqueCount
operator|.
name|getAndIncrement
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|Group
specifier|public
name|Group
parameter_list|(
name|double
name|x
parameter_list|,
name|int
name|id
parameter_list|)
block|{
name|this
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|start
argument_list|(
name|x
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
DECL|method|Group
specifier|public
name|Group
parameter_list|(
name|double
name|x
parameter_list|,
name|int
name|id
parameter_list|,
name|boolean
name|record
parameter_list|)
block|{
name|this
argument_list|(
name|record
argument_list|)
expr_stmt|;
name|start
argument_list|(
name|x
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
DECL|method|start
specifier|private
name|void
name|start
parameter_list|(
name|double
name|x
parameter_list|,
name|int
name|id
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|add
argument_list|(
name|x
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|double
name|x
parameter_list|,
name|int
name|w
parameter_list|)
block|{
if|if
condition|(
name|actualData
operator|!=
literal|null
condition|)
block|{
name|actualData
operator|.
name|add
argument_list|(
name|x
argument_list|)
expr_stmt|;
block|}
name|count
operator|+=
name|w
expr_stmt|;
name|centroid
operator|+=
name|w
operator|*
operator|(
name|x
operator|-
name|centroid
operator|)
operator|/
name|count
expr_stmt|;
block|}
DECL|method|mean
specifier|public
name|double
name|mean
parameter_list|()
block|{
return|return
name|centroid
return|;
block|}
DECL|method|count
specifier|public
name|int
name|count
parameter_list|()
block|{
return|return
name|count
return|;
block|}
DECL|method|id
specifier|public
name|int
name|id
parameter_list|()
block|{
return|return
name|id
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"Group{"
operator|+
literal|"centroid="
operator|+
name|centroid
operator|+
literal|", count="
operator|+
name|count
operator|+
literal|'}'
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
name|id
return|;
block|}
annotation|@
name|Override
DECL|method|compareTo
specifier|public
name|int
name|compareTo
parameter_list|(
name|Group
name|o
parameter_list|)
block|{
name|int
name|r
init|=
name|Double
operator|.
name|compare
argument_list|(
name|centroid
argument_list|,
name|o
operator|.
name|centroid
argument_list|)
decl_stmt|;
if|if
condition|(
name|r
operator|==
literal|0
condition|)
block|{
name|r
operator|=
name|id
operator|-
name|o
operator|.
name|id
expr_stmt|;
block|}
return|return
name|r
return|;
block|}
DECL|method|data
specifier|public
name|Iterable
argument_list|<
name|?
extends|extends
name|Double
argument_list|>
name|data
parameter_list|()
block|{
return|return
name|actualData
return|;
block|}
DECL|method|createWeighted
specifier|public
specifier|static
name|Group
name|createWeighted
parameter_list|(
name|double
name|x
parameter_list|,
name|int
name|w
parameter_list|,
name|Iterable
argument_list|<
name|?
extends|extends
name|Double
argument_list|>
name|data
parameter_list|)
block|{
name|Group
name|r
init|=
operator|new
name|Group
argument_list|(
name|data
operator|!=
literal|null
argument_list|)
decl_stmt|;
name|r
operator|.
name|add
argument_list|(
name|x
argument_list|,
name|w
argument_list|,
name|data
argument_list|)
expr_stmt|;
return|return
name|r
return|;
block|}
DECL|method|add
specifier|private
name|void
name|add
parameter_list|(
name|double
name|x
parameter_list|,
name|int
name|w
parameter_list|,
name|Iterable
argument_list|<
name|?
extends|extends
name|Double
argument_list|>
name|data
parameter_list|)
block|{
if|if
condition|(
name|actualData
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|data
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Double
name|old
range|:
name|data
control|)
block|{
name|actualData
operator|.
name|add
argument_list|(
name|old
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|actualData
operator|.
name|add
argument_list|(
name|x
argument_list|)
expr_stmt|;
block|}
block|}
name|count
operator|+=
name|w
expr_stmt|;
name|centroid
operator|+=
name|w
operator|*
operator|(
name|x
operator|-
name|centroid
operator|)
operator|/
name|count
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

