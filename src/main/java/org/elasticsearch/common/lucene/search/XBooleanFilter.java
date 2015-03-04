begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.common.lucene.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|search
package|;
end_package

begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to You under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|LeafReaderContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|queries
operator|.
name|FilterClause
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|BitsFilteredDocIdSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|BooleanClause
operator|.
name|Occur
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|DocIdSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|DocIdSetIterator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Filter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|BitDocIdSet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Bits
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|CollectionUtil
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
name|lucene
operator|.
name|docset
operator|.
name|AllDocIdSet
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
name|lucene
operator|.
name|docset
operator|.
name|AndDocIdSet
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
name|lucene
operator|.
name|docset
operator|.
name|DocIdSets
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
name|lucene
operator|.
name|docset
operator|.
name|NotDocIdSet
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
name|lucene
operator|.
name|docset
operator|.
name|OrDocIdSet
operator|.
name|OrBits
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|Comparator
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
comment|/**  * Similar to {@link org.apache.lucene.queries.BooleanFilter}.  *<p/>  * Our own variance mainly differs by the fact that we pass the acceptDocs down to the filters  * and don't filter based on them at the end. Our logic is a bit different, and we filter based on that  * at the top level filter chain.  */
end_comment

begin_class
DECL|class|XBooleanFilter
specifier|public
class|class
name|XBooleanFilter
extends|extends
name|Filter
implements|implements
name|Iterable
argument_list|<
name|FilterClause
argument_list|>
block|{
DECL|field|COST_DESCENDING
specifier|private
specifier|static
specifier|final
name|Comparator
argument_list|<
name|DocIdSetIterator
argument_list|>
name|COST_DESCENDING
init|=
operator|new
name|Comparator
argument_list|<
name|DocIdSetIterator
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|DocIdSetIterator
name|o1
parameter_list|,
name|DocIdSetIterator
name|o2
parameter_list|)
block|{
return|return
name|Long
operator|.
name|compare
argument_list|(
name|o2
operator|.
name|cost
argument_list|()
argument_list|,
name|o1
operator|.
name|cost
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|COST_ASCENDING
specifier|private
specifier|static
specifier|final
name|Comparator
argument_list|<
name|DocIdSetIterator
argument_list|>
name|COST_ASCENDING
init|=
operator|new
name|Comparator
argument_list|<
name|DocIdSetIterator
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|DocIdSetIterator
name|o1
parameter_list|,
name|DocIdSetIterator
name|o2
parameter_list|)
block|{
return|return
name|Long
operator|.
name|compare
argument_list|(
name|o1
operator|.
name|cost
argument_list|()
argument_list|,
name|o2
operator|.
name|cost
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
DECL|field|clauses
specifier|final
name|List
argument_list|<
name|FilterClause
argument_list|>
name|clauses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**      * Returns the a DocIdSetIterator representing the Boolean composition      * of the filters that have been added.      */
annotation|@
name|Override
DECL|method|getDocIdSet
specifier|public
name|DocIdSet
name|getDocIdSet
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|,
name|Bits
name|acceptDocs
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|int
name|maxDoc
init|=
name|context
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
decl_stmt|;
comment|// the 0-clauses case is ambiguous because an empty OR filter should return nothing
comment|// while an empty AND filter should return all docs, so we handle this case explicitely
if|if
condition|(
name|clauses
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// optimize single case...
if|if
condition|(
name|clauses
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|FilterClause
name|clause
init|=
name|clauses
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|DocIdSet
name|set
init|=
name|clause
operator|.
name|getFilter
argument_list|()
operator|.
name|getDocIdSet
argument_list|(
name|context
argument_list|,
name|acceptDocs
argument_list|)
decl_stmt|;
if|if
condition|(
name|clause
operator|.
name|getOccur
argument_list|()
operator|==
name|Occur
operator|.
name|MUST_NOT
condition|)
block|{
if|if
condition|(
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|set
argument_list|)
condition|)
block|{
return|return
operator|new
name|AllDocIdSet
argument_list|(
name|maxDoc
argument_list|)
return|;
block|}
else|else
block|{
return|return
operator|new
name|NotDocIdSet
argument_list|(
name|set
argument_list|,
name|maxDoc
argument_list|)
return|;
block|}
block|}
comment|// SHOULD or MUST, just return the set...
if|if
condition|(
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|set
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|set
return|;
block|}
comment|// We have several clauses, try to organize things to make it easier to process
name|List
argument_list|<
name|DocIdSetIterator
argument_list|>
name|shouldIterators
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Bits
argument_list|>
name|shouldBits
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|boolean
name|hasShouldClauses
init|=
literal|false
decl_stmt|;
name|List
argument_list|<
name|DocIdSetIterator
argument_list|>
name|requiredIterators
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|DocIdSetIterator
argument_list|>
name|excludedIterators
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Bits
argument_list|>
name|requiredBits
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Bits
argument_list|>
name|excludedBits
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|FilterClause
name|clause
range|:
name|clauses
control|)
block|{
name|DocIdSet
name|set
init|=
name|clause
operator|.
name|getFilter
argument_list|()
operator|.
name|getDocIdSet
argument_list|(
name|context
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|DocIdSetIterator
name|it
init|=
literal|null
decl_stmt|;
name|Bits
name|bits
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|DocIdSets
operator|.
name|isEmpty
argument_list|(
name|set
argument_list|)
operator|==
literal|false
condition|)
block|{
name|it
operator|=
name|set
operator|.
name|iterator
argument_list|()
expr_stmt|;
if|if
condition|(
name|it
operator|!=
literal|null
condition|)
block|{
name|bits
operator|=
name|set
operator|.
name|bits
argument_list|()
expr_stmt|;
block|}
block|}
switch|switch
condition|(
name|clause
operator|.
name|getOccur
argument_list|()
condition|)
block|{
case|case
name|SHOULD
case|:
name|hasShouldClauses
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|it
operator|==
literal|null
condition|)
block|{
comment|// continue, but we recorded that there is at least one should clause
comment|// so that if all iterators are null we know that nothing matches this
comment|// filter since at least one SHOULD clause needs to match
block|}
elseif|else
if|if
condition|(
name|bits
operator|!=
literal|null
operator|&&
name|DocIdSets
operator|.
name|isBroken
argument_list|(
name|it
argument_list|)
condition|)
block|{
name|shouldBits
operator|.
name|add
argument_list|(
name|bits
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|shouldIterators
operator|.
name|add
argument_list|(
name|it
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|MUST
case|:
if|if
condition|(
name|it
operator|==
literal|null
condition|)
block|{
comment|// no documents matched a clause that is compulsory, then nothing matches at all
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|bits
operator|!=
literal|null
operator|&&
name|DocIdSets
operator|.
name|isBroken
argument_list|(
name|it
argument_list|)
condition|)
block|{
name|requiredBits
operator|.
name|add
argument_list|(
name|bits
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|requiredIterators
operator|.
name|add
argument_list|(
name|it
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|MUST_NOT
case|:
if|if
condition|(
name|it
operator|==
literal|null
condition|)
block|{
comment|// ignore
block|}
elseif|else
if|if
condition|(
name|bits
operator|!=
literal|null
operator|&&
name|DocIdSets
operator|.
name|isBroken
argument_list|(
name|it
argument_list|)
condition|)
block|{
name|excludedBits
operator|.
name|add
argument_list|(
name|bits
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|excludedIterators
operator|.
name|add
argument_list|(
name|it
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
comment|// Since BooleanFilter requires that at least one SHOULD clause matches,
comment|// transform the SHOULD clauses into a MUST clause
if|if
condition|(
name|hasShouldClauses
condition|)
block|{
if|if
condition|(
name|shouldIterators
operator|.
name|isEmpty
argument_list|()
operator|&&
name|shouldBits
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// we had should clauses, but they all produced empty sets
comment|// yet BooleanFilter requires that at least one clause matches
comment|// so it means we do not match anything
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|shouldIterators
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
name|shouldBits
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|requiredIterators
operator|.
name|add
argument_list|(
name|shouldIterators
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// apply high-cardinality should clauses first
name|CollectionUtil
operator|.
name|timSort
argument_list|(
name|shouldIterators
argument_list|,
name|COST_DESCENDING
argument_list|)
expr_stmt|;
name|BitDocIdSet
operator|.
name|Builder
name|shouldBuilder
init|=
literal|null
decl_stmt|;
for|for
control|(
name|DocIdSetIterator
name|it
range|:
name|shouldIterators
control|)
block|{
if|if
condition|(
name|shouldBuilder
operator|==
literal|null
condition|)
block|{
name|shouldBuilder
operator|=
operator|new
name|BitDocIdSet
operator|.
name|Builder
argument_list|(
name|maxDoc
argument_list|)
expr_stmt|;
block|}
name|shouldBuilder
operator|.
name|or
argument_list|(
name|it
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|shouldBuilder
operator|!=
literal|null
operator|&&
name|shouldBits
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
comment|// we have both iterators and bits, there is no way to compute
comment|// the union efficiently, so we just transform the iterators into
comment|// bits
comment|// add first since these are fast bits
name|shouldBits
operator|.
name|add
argument_list|(
literal|0
argument_list|,
name|shouldBuilder
operator|.
name|build
argument_list|()
operator|.
name|bits
argument_list|()
argument_list|)
expr_stmt|;
name|shouldBuilder
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|shouldBuilder
operator|==
literal|null
condition|)
block|{
comment|// only bits
assert|assert
name|shouldBits
operator|.
name|size
argument_list|()
operator|>=
literal|1
assert|;
if|if
condition|(
name|shouldBits
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|requiredBits
operator|.
name|add
argument_list|(
name|shouldBits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|requiredBits
operator|.
name|add
argument_list|(
operator|new
name|OrBits
argument_list|(
name|shouldBits
operator|.
name|toArray
argument_list|(
operator|new
name|Bits
index|[
name|shouldBits
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
assert|assert
name|shouldBits
operator|.
name|isEmpty
argument_list|()
assert|;
comment|// only iterators, we can add the merged iterator to the list of required iterators
name|requiredIterators
operator|.
name|add
argument_list|(
name|shouldBuilder
operator|.
name|build
argument_list|()
operator|.
name|iterator
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
assert|assert
name|shouldIterators
operator|.
name|isEmpty
argument_list|()
assert|;
assert|assert
name|shouldBits
operator|.
name|isEmpty
argument_list|()
assert|;
block|}
comment|// From now on, we don't have to care about SHOULD clauses anymore since we upgraded
comment|// them to required clauses (if necessary)
comment|// cheap iterators first to make intersection faster
name|CollectionUtil
operator|.
name|timSort
argument_list|(
name|requiredIterators
argument_list|,
name|COST_ASCENDING
argument_list|)
expr_stmt|;
name|CollectionUtil
operator|.
name|timSort
argument_list|(
name|excludedIterators
argument_list|,
name|COST_ASCENDING
argument_list|)
expr_stmt|;
comment|// Intersect iterators
name|BitDocIdSet
operator|.
name|Builder
name|res
init|=
literal|null
decl_stmt|;
for|for
control|(
name|DocIdSetIterator
name|iterator
range|:
name|requiredIterators
control|)
block|{
if|if
condition|(
name|res
operator|==
literal|null
condition|)
block|{
name|res
operator|=
operator|new
name|BitDocIdSet
operator|.
name|Builder
argument_list|(
name|maxDoc
argument_list|)
expr_stmt|;
name|res
operator|.
name|or
argument_list|(
name|iterator
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|res
operator|.
name|and
argument_list|(
name|iterator
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|DocIdSetIterator
name|iterator
range|:
name|excludedIterators
control|)
block|{
if|if
condition|(
name|res
operator|==
literal|null
condition|)
block|{
name|res
operator|=
operator|new
name|BitDocIdSet
operator|.
name|Builder
argument_list|(
name|maxDoc
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
name|res
operator|.
name|andNot
argument_list|(
name|iterator
argument_list|)
expr_stmt|;
block|}
comment|// Transform the excluded bits into required bits
if|if
condition|(
name|excludedBits
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|Bits
name|excluded
decl_stmt|;
if|if
condition|(
name|excludedBits
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|excluded
operator|=
name|excludedBits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|excluded
operator|=
operator|new
name|OrBits
argument_list|(
name|excludedBits
operator|.
name|toArray
argument_list|(
operator|new
name|Bits
index|[
name|excludedBits
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|requiredBits
operator|.
name|add
argument_list|(
operator|new
name|NotDocIdSet
operator|.
name|NotBits
argument_list|(
name|excluded
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// The only thing left to do is to intersect 'res' with 'requiredBits'
comment|// the main doc id set that will drive iteration
name|DocIdSet
name|main
decl_stmt|;
if|if
condition|(
name|res
operator|==
literal|null
condition|)
block|{
name|main
operator|=
operator|new
name|AllDocIdSet
argument_list|(
name|maxDoc
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|main
operator|=
name|res
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
comment|// apply accepted docs and compute the bits to filter with
comment|// accepted docs are added first since they are fast and will help not computing anything on deleted docs
if|if
condition|(
name|acceptDocs
operator|!=
literal|null
condition|)
block|{
name|requiredBits
operator|.
name|add
argument_list|(
literal|0
argument_list|,
name|acceptDocs
argument_list|)
expr_stmt|;
block|}
comment|// the random-access filter that we will apply to 'main'
name|Bits
name|filter
decl_stmt|;
if|if
condition|(
name|requiredBits
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|filter
operator|=
literal|null
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|requiredBits
operator|.
name|size
argument_list|()
operator|==
literal|1
condition|)
block|{
name|filter
operator|=
name|requiredBits
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|filter
operator|=
operator|new
name|AndDocIdSet
operator|.
name|AndBits
argument_list|(
name|requiredBits
operator|.
name|toArray
argument_list|(
operator|new
name|Bits
index|[
name|requiredBits
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|BitsFilteredDocIdSet
operator|.
name|wrap
argument_list|(
name|main
argument_list|,
name|filter
argument_list|)
return|;
block|}
comment|/**      * Adds a new FilterClause to the Boolean Filter container      *      * @param filterClause A FilterClause object containing a Filter and an Occur parameter      */
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|FilterClause
name|filterClause
parameter_list|)
block|{
name|clauses
operator|.
name|add
argument_list|(
name|filterClause
argument_list|)
expr_stmt|;
block|}
DECL|method|add
specifier|public
specifier|final
name|void
name|add
parameter_list|(
name|Filter
name|filter
parameter_list|,
name|Occur
name|occur
parameter_list|)
block|{
name|add
argument_list|(
operator|new
name|FilterClause
argument_list|(
name|filter
argument_list|,
name|occur
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the list of clauses      */
DECL|method|clauses
specifier|public
name|List
argument_list|<
name|FilterClause
argument_list|>
name|clauses
parameter_list|()
block|{
return|return
name|clauses
return|;
block|}
comment|/**      * Returns an iterator on the clauses in this query. It implements the {@link Iterable} interface to      * make it possible to do:      *<pre class="prettyprint">for (FilterClause clause : booleanFilter) {}</pre>      */
annotation|@
name|Override
DECL|method|iterator
specifier|public
specifier|final
name|Iterator
argument_list|<
name|FilterClause
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|clauses
argument_list|()
operator|.
name|iterator
argument_list|()
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
name|obj
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|obj
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
operator|(
name|obj
operator|==
literal|null
operator|)
operator|||
operator|(
name|obj
operator|.
name|getClass
argument_list|()
operator|!=
name|this
operator|.
name|getClass
argument_list|()
operator|)
condition|)
block|{
return|return
literal|false
return|;
block|}
specifier|final
name|XBooleanFilter
name|other
init|=
operator|(
name|XBooleanFilter
operator|)
name|obj
decl_stmt|;
return|return
name|clauses
operator|.
name|equals
argument_list|(
name|other
operator|.
name|clauses
argument_list|)
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
literal|657153718
operator|^
name|clauses
operator|.
name|hashCode
argument_list|()
return|;
block|}
comment|/**      * Prints a user-readable version of this Filter.      */
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|(
name|String
name|field
parameter_list|)
block|{
specifier|final
name|StringBuilder
name|buffer
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"BooleanFilter("
argument_list|)
decl_stmt|;
specifier|final
name|int
name|minLen
init|=
name|buffer
operator|.
name|length
argument_list|()
decl_stmt|;
for|for
control|(
specifier|final
name|FilterClause
name|c
range|:
name|clauses
control|)
block|{
if|if
condition|(
name|buffer
operator|.
name|length
argument_list|()
operator|>
name|minLen
condition|)
block|{
name|buffer
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
block|}
name|buffer
operator|.
name|append
argument_list|(
name|c
argument_list|)
expr_stmt|;
block|}
return|return
name|buffer
operator|.
name|append
argument_list|(
literal|')'
argument_list|)
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|class|ResultClause
specifier|static
class|class
name|ResultClause
block|{
DECL|field|docIdSet
specifier|public
specifier|final
name|DocIdSet
name|docIdSet
decl_stmt|;
DECL|field|bits
specifier|public
specifier|final
name|Bits
name|bits
decl_stmt|;
DECL|field|clause
specifier|public
specifier|final
name|FilterClause
name|clause
decl_stmt|;
DECL|field|docIdSetIterator
name|DocIdSetIterator
name|docIdSetIterator
decl_stmt|;
DECL|method|ResultClause
name|ResultClause
parameter_list|(
name|DocIdSet
name|docIdSet
parameter_list|,
name|Bits
name|bits
parameter_list|,
name|FilterClause
name|clause
parameter_list|)
block|{
name|this
operator|.
name|docIdSet
operator|=
name|docIdSet
expr_stmt|;
name|this
operator|.
name|bits
operator|=
name|bits
expr_stmt|;
name|this
operator|.
name|clause
operator|=
name|clause
expr_stmt|;
block|}
comment|/**          * @return An iterator, but caches it for subsequent usage. Don't use if iterator is consumed in one invocation.          */
DECL|method|iterator
name|DocIdSetIterator
name|iterator
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|docIdSetIterator
operator|!=
literal|null
condition|)
block|{
return|return
name|docIdSetIterator
return|;
block|}
else|else
block|{
return|return
name|docIdSetIterator
operator|=
name|docIdSet
operator|.
name|iterator
argument_list|()
return|;
block|}
block|}
block|}
DECL|method|iteratorMatch
specifier|static
name|boolean
name|iteratorMatch
parameter_list|(
name|DocIdSetIterator
name|docIdSetIterator
parameter_list|,
name|int
name|target
parameter_list|)
throws|throws
name|IOException
block|{
assert|assert
name|docIdSetIterator
operator|!=
literal|null
assert|;
name|int
name|current
init|=
name|docIdSetIterator
operator|.
name|docID
argument_list|()
decl_stmt|;
if|if
condition|(
name|current
operator|==
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
operator|||
name|target
operator|<
name|current
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
if|if
condition|(
name|current
operator|==
name|target
condition|)
block|{
return|return
literal|true
return|;
block|}
else|else
block|{
return|return
name|docIdSetIterator
operator|.
name|advance
argument_list|(
name|target
argument_list|)
operator|==
name|target
return|;
block|}
block|}
block|}
block|}
end_class

end_unit

