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
name|AtomicReader
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
name|index
operator|.
name|AtomicReaderContext
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
name|FixedBitSet
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
argument_list|<
name|FilterClause
argument_list|>
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
name|AtomicReaderContext
name|context
parameter_list|,
name|Bits
name|acceptDocs
parameter_list|)
throws|throws
name|IOException
block|{
name|FixedBitSet
name|res
init|=
literal|null
decl_stmt|;
specifier|final
name|AtomicReader
name|reader
init|=
name|context
operator|.
name|reader
argument_list|()
decl_stmt|;
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
name|reader
operator|.
name|maxDoc
argument_list|()
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
name|reader
operator|.
name|maxDoc
argument_list|()
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
comment|// first, go over and see if we can shortcut the execution
comment|// and gather Bits if we need to
name|List
argument_list|<
name|ResultClause
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|ResultClause
argument_list|>
argument_list|(
name|clauses
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
name|boolean
name|hasShouldClauses
init|=
literal|false
decl_stmt|;
name|boolean
name|hasNonEmptyShouldClause
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|clauses
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|FilterClause
name|clause
init|=
name|clauses
operator|.
name|get
argument_list|(
name|i
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
name|MUST
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
literal|null
return|;
block|}
block|}
elseif|else
if|if
condition|(
name|clause
operator|.
name|getOccur
argument_list|()
operator|==
name|Occur
operator|.
name|SHOULD
condition|)
block|{
name|hasShouldClauses
operator|=
literal|true
expr_stmt|;
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
continue|continue;
block|}
name|hasNonEmptyShouldClause
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
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
comment|// we mark empty ones as null for must_not, handle it in the next run...
name|results
operator|.
name|add
argument_list|(
operator|new
name|ResultClause
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|,
name|clause
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
block|}
name|Bits
name|bits
init|=
literal|null
decl_stmt|;
if|if
condition|(
operator|!
name|DocIdSets
operator|.
name|isFastIterator
argument_list|(
name|set
argument_list|)
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
name|results
operator|.
name|add
argument_list|(
operator|new
name|ResultClause
argument_list|(
name|set
argument_list|,
name|bits
argument_list|,
name|clause
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hasShouldClauses
operator|&&
operator|!
name|hasNonEmptyShouldClause
condition|)
block|{
return|return
literal|null
return|;
block|}
comment|// now, go over the clauses and apply the "fast" ones...
name|boolean
name|hasBits
init|=
literal|false
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|results
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ResultClause
name|clause
init|=
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
comment|// we apply bits in based ones (slow) in the second run
if|if
condition|(
name|clause
operator|.
name|bits
operator|!=
literal|null
condition|)
block|{
name|hasBits
operator|=
literal|true
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|clause
operator|.
name|clause
operator|.
name|getOccur
argument_list|()
operator|==
name|Occur
operator|.
name|SHOULD
condition|)
block|{
name|DocIdSetIterator
name|it
init|=
name|clause
operator|.
name|docIdSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|it
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
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
name|FixedBitSet
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|res
operator|.
name|or
argument_list|(
name|it
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|clause
operator|.
name|clause
operator|.
name|getOccur
argument_list|()
operator|==
name|Occur
operator|.
name|MUST
condition|)
block|{
name|DocIdSetIterator
name|it
init|=
name|clause
operator|.
name|docIdSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|it
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
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
name|FixedBitSet
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
name|res
operator|.
name|or
argument_list|(
name|it
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|res
operator|.
name|and
argument_list|(
name|it
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|clause
operator|.
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
name|res
operator|==
literal|null
condition|)
block|{
name|res
operator|=
operator|new
name|FixedBitSet
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
name|res
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
comment|// NOTE: may set bits on deleted docs
block|}
if|if
condition|(
name|clause
operator|.
name|docIdSet
operator|!=
literal|null
condition|)
block|{
name|DocIdSetIterator
name|it
init|=
name|clause
operator|.
name|docIdSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|it
operator|!=
literal|null
condition|)
block|{
name|res
operator|.
name|andNot
argument_list|(
name|it
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
if|if
condition|(
operator|!
name|hasBits
condition|)
block|{
return|return
name|res
return|;
block|}
comment|// we have some clauses with bits, apply them...
comment|// we let the "res" drive the computation, and check Bits for that
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|results
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|ResultClause
name|clause
init|=
name|results
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
comment|// we apply bits in based ones (slow) in the second run
if|if
condition|(
name|clause
operator|.
name|bits
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|clause
operator|.
name|clause
operator|.
name|getOccur
argument_list|()
operator|==
name|Occur
operator|.
name|SHOULD
condition|)
block|{
comment|// TODO: we should let res drive it, and check on all unset bits on it with Bits
name|DocIdSetIterator
name|it
init|=
name|clause
operator|.
name|docIdSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|it
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
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
name|FixedBitSet
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|res
operator|.
name|or
argument_list|(
name|it
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|clause
operator|.
name|clause
operator|.
name|getOccur
argument_list|()
operator|==
name|Occur
operator|.
name|MUST
condition|)
block|{
if|if
condition|(
name|res
operator|==
literal|null
condition|)
block|{
comment|// nothing we can do, just or it...
name|res
operator|=
operator|new
name|FixedBitSet
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
name|DocIdSetIterator
name|it
init|=
name|clause
operator|.
name|docIdSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|it
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|res
operator|.
name|or
argument_list|(
name|it
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Bits
name|bits
init|=
name|clause
operator|.
name|bits
decl_stmt|;
comment|// use the "res" to drive the iteration
name|DocIdSetIterator
name|it
init|=
name|res
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|doc
init|=
name|it
operator|.
name|nextDoc
argument_list|()
init|;
name|doc
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|;
name|doc
operator|=
name|it
operator|.
name|nextDoc
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|bits
operator|.
name|get
argument_list|(
name|doc
argument_list|)
condition|)
block|{
name|res
operator|.
name|clear
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|clause
operator|.
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
name|res
operator|==
literal|null
condition|)
block|{
name|res
operator|=
operator|new
name|FixedBitSet
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
name|res
operator|.
name|set
argument_list|(
literal|0
argument_list|,
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|)
expr_stmt|;
comment|// NOTE: may set bits on deleted docs
name|DocIdSetIterator
name|it
init|=
name|clause
operator|.
name|docIdSet
operator|.
name|iterator
argument_list|()
decl_stmt|;
if|if
condition|(
name|it
operator|!=
literal|null
condition|)
block|{
name|res
operator|.
name|andNot
argument_list|(
name|it
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|Bits
name|bits
init|=
name|clause
operator|.
name|bits
decl_stmt|;
comment|// let res drive the iteration
name|DocIdSetIterator
name|it
init|=
name|res
operator|.
name|iterator
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|doc
init|=
name|it
operator|.
name|nextDoc
argument_list|()
init|;
name|doc
operator|!=
name|DocIdSetIterator
operator|.
name|NO_MORE_DOCS
condition|;
name|doc
operator|=
name|it
operator|.
name|nextDoc
argument_list|()
control|)
block|{
if|if
condition|(
name|bits
operator|.
name|get
argument_list|(
name|doc
argument_list|)
condition|)
block|{
name|res
operator|.
name|clear
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
return|return
name|res
return|;
block|}
DECL|method|getDISI
specifier|private
specifier|static
name|DocIdSetIterator
name|getDISI
parameter_list|(
name|Filter
name|filter
parameter_list|,
name|AtomicReaderContext
name|context
parameter_list|,
name|Bits
name|acceptDocs
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|DocIdSet
name|set
init|=
name|filter
operator|.
name|getDocIdSet
argument_list|(
name|context
argument_list|,
name|acceptDocs
argument_list|)
decl_stmt|;
return|return
operator|(
name|set
operator|==
literal|null
operator|||
name|set
operator|==
name|DocIdSet
operator|.
name|EMPTY_DOCIDSET
operator|)
condition|?
literal|null
else|:
name|set
operator|.
name|iterator
argument_list|()
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
parameter_list|()
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
block|}
block|}
end_class

end_unit

