begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.terms.strings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|terms
operator|.
name|strings
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
name|util
operator|.
name|BytesRef
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
name|CharsRef
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
name|PriorityQueue
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
name|UnicodeUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cache
operator|.
name|recycler
operator|.
name|CacheRecycler
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
name|collect
operator|.
name|BoundedTreeSet
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
name|util
operator|.
name|IntArray
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
name|util
operator|.
name|IntArrays
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|BytesValues
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|IndexFieldData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|ordinals
operator|.
name|Ordinals
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|FacetExecutor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|InternalFacet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|terms
operator|.
name|TermsFacet
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|terms
operator|.
name|support
operator|.
name|EntryPriorityQueue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|internal
operator|.
name|SearchContext
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
name|Arrays
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
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TermsStringOrdinalsFacetExecutor
specifier|public
class|class
name|TermsStringOrdinalsFacetExecutor
extends|extends
name|FacetExecutor
block|{
DECL|field|indexFieldData
specifier|private
specifier|final
name|IndexFieldData
operator|.
name|WithOrdinals
name|indexFieldData
decl_stmt|;
DECL|field|cacheRecycler
specifier|final
name|CacheRecycler
name|cacheRecycler
decl_stmt|;
DECL|field|comparatorType
specifier|private
specifier|final
name|TermsFacet
operator|.
name|ComparatorType
name|comparatorType
decl_stmt|;
DECL|field|size
specifier|private
specifier|final
name|int
name|size
decl_stmt|;
DECL|field|shardSize
specifier|private
specifier|final
name|int
name|shardSize
decl_stmt|;
DECL|field|minCount
specifier|private
specifier|final
name|int
name|minCount
decl_stmt|;
DECL|field|excluded
specifier|private
specifier|final
name|ImmutableSet
argument_list|<
name|BytesRef
argument_list|>
name|excluded
decl_stmt|;
DECL|field|matcher
specifier|private
specifier|final
name|Matcher
name|matcher
decl_stmt|;
DECL|field|ordinalsCacheAbove
specifier|final
name|int
name|ordinalsCacheAbove
decl_stmt|;
DECL|field|aggregators
specifier|final
name|List
argument_list|<
name|ReaderAggregator
argument_list|>
name|aggregators
decl_stmt|;
DECL|field|missing
name|long
name|missing
decl_stmt|;
DECL|field|total
name|long
name|total
decl_stmt|;
DECL|method|TermsStringOrdinalsFacetExecutor
specifier|public
name|TermsStringOrdinalsFacetExecutor
parameter_list|(
name|IndexFieldData
operator|.
name|WithOrdinals
name|indexFieldData
parameter_list|,
name|int
name|size
parameter_list|,
name|int
name|shardSize
parameter_list|,
name|TermsFacet
operator|.
name|ComparatorType
name|comparatorType
parameter_list|,
name|boolean
name|allTerms
parameter_list|,
name|SearchContext
name|context
parameter_list|,
name|ImmutableSet
argument_list|<
name|BytesRef
argument_list|>
name|excluded
parameter_list|,
name|Pattern
name|pattern
parameter_list|,
name|int
name|ordinalsCacheAbove
parameter_list|)
block|{
name|this
operator|.
name|indexFieldData
operator|=
name|indexFieldData
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|shardSize
operator|=
name|shardSize
expr_stmt|;
name|this
operator|.
name|comparatorType
operator|=
name|comparatorType
expr_stmt|;
name|this
operator|.
name|ordinalsCacheAbove
operator|=
name|ordinalsCacheAbove
expr_stmt|;
if|if
condition|(
name|excluded
operator|==
literal|null
operator|||
name|excluded
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|this
operator|.
name|excluded
operator|=
literal|null
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|excluded
operator|=
name|excluded
expr_stmt|;
block|}
name|this
operator|.
name|matcher
operator|=
name|pattern
operator|!=
literal|null
condition|?
name|pattern
operator|.
name|matcher
argument_list|(
literal|""
argument_list|)
else|:
literal|null
expr_stmt|;
comment|// minCount is offset by -1
if|if
condition|(
name|allTerms
condition|)
block|{
name|minCount
operator|=
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|minCount
operator|=
literal|0
expr_stmt|;
block|}
name|this
operator|.
name|cacheRecycler
operator|=
name|context
operator|.
name|cacheRecycler
argument_list|()
expr_stmt|;
name|this
operator|.
name|aggregators
operator|=
operator|new
name|ArrayList
argument_list|<
name|ReaderAggregator
argument_list|>
argument_list|(
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getIndexReader
argument_list|()
operator|.
name|leaves
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|collector
specifier|public
name|Collector
name|collector
parameter_list|()
block|{
return|return
operator|new
name|Collector
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|buildFacet
specifier|public
name|InternalFacet
name|buildFacet
parameter_list|(
name|String
name|facetName
parameter_list|)
block|{
specifier|final
name|CharsRef
name|spare
init|=
operator|new
name|CharsRef
argument_list|()
decl_stmt|;
name|AggregatorPriorityQueue
name|queue
init|=
operator|new
name|AggregatorPriorityQueue
argument_list|(
name|aggregators
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ReaderAggregator
name|aggregator
range|:
name|aggregators
control|)
block|{
if|if
condition|(
name|aggregator
operator|.
name|nextPosition
argument_list|()
condition|)
block|{
name|queue
operator|.
name|add
argument_list|(
name|aggregator
argument_list|)
expr_stmt|;
block|}
block|}
comment|// YACK, we repeat the same logic, but once with an optimizer priority queue for smaller sizes
if|if
condition|(
name|shardSize
operator|<
name|EntryPriorityQueue
operator|.
name|LIMIT
condition|)
block|{
comment|// optimize to use priority size
name|EntryPriorityQueue
name|ordered
init|=
operator|new
name|EntryPriorityQueue
argument_list|(
name|shardSize
argument_list|,
name|comparatorType
operator|.
name|comparator
argument_list|()
argument_list|)
decl_stmt|;
while|while
condition|(
name|queue
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|ReaderAggregator
name|agg
init|=
name|queue
operator|.
name|top
argument_list|()
decl_stmt|;
name|BytesRef
name|value
init|=
name|agg
operator|.
name|copyCurrent
argument_list|()
decl_stmt|;
comment|// we need to makeSafe it, since we end up pushing it... (can we get around this?)
name|int
name|count
init|=
literal|0
decl_stmt|;
do|do
block|{
name|count
operator|+=
name|agg
operator|.
name|counts
operator|.
name|get
argument_list|(
name|agg
operator|.
name|position
argument_list|)
expr_stmt|;
if|if
condition|(
name|agg
operator|.
name|nextPosition
argument_list|()
condition|)
block|{
name|agg
operator|=
name|queue
operator|.
name|updateTop
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// we are done with this reader
name|queue
operator|.
name|pop
argument_list|()
expr_stmt|;
name|agg
operator|=
name|queue
operator|.
name|top
argument_list|()
expr_stmt|;
block|}
block|}
do|while
condition|(
name|agg
operator|!=
literal|null
operator|&&
name|value
operator|.
name|equals
argument_list|(
name|agg
operator|.
name|current
argument_list|)
condition|)
do|;
if|if
condition|(
name|count
operator|>
name|minCount
condition|)
block|{
if|if
condition|(
name|excluded
operator|!=
literal|null
operator|&&
name|excluded
operator|.
name|contains
argument_list|(
name|value
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|matcher
operator|!=
literal|null
condition|)
block|{
name|UnicodeUtil
operator|.
name|UTF8toUTF16
argument_list|(
name|value
argument_list|,
name|spare
argument_list|)
expr_stmt|;
assert|assert
name|spare
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|value
operator|.
name|utf8ToString
argument_list|()
argument_list|)
assert|;
if|if
condition|(
operator|!
name|matcher
operator|.
name|reset
argument_list|(
name|spare
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
continue|continue;
block|}
block|}
name|InternalStringTermsFacet
operator|.
name|TermEntry
name|entry
init|=
operator|new
name|InternalStringTermsFacet
operator|.
name|TermEntry
argument_list|(
name|value
argument_list|,
name|count
argument_list|)
decl_stmt|;
name|ordered
operator|.
name|insertWithOverflow
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
block|}
name|InternalStringTermsFacet
operator|.
name|TermEntry
index|[]
name|list
init|=
operator|new
name|InternalStringTermsFacet
operator|.
name|TermEntry
index|[
name|ordered
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|ordered
operator|.
name|size
argument_list|()
operator|-
literal|1
init|;
name|i
operator|>=
literal|0
condition|;
name|i
operator|--
control|)
block|{
name|list
index|[
name|i
index|]
operator|=
operator|(
name|InternalStringTermsFacet
operator|.
name|TermEntry
operator|)
name|ordered
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
return|return
operator|new
name|InternalStringTermsFacet
argument_list|(
name|facetName
argument_list|,
name|comparatorType
argument_list|,
name|size
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|list
argument_list|)
argument_list|,
name|missing
argument_list|,
name|total
argument_list|)
return|;
block|}
name|BoundedTreeSet
argument_list|<
name|InternalStringTermsFacet
operator|.
name|TermEntry
argument_list|>
name|ordered
init|=
operator|new
name|BoundedTreeSet
argument_list|<
name|InternalStringTermsFacet
operator|.
name|TermEntry
argument_list|>
argument_list|(
name|comparatorType
operator|.
name|comparator
argument_list|()
argument_list|,
name|shardSize
argument_list|)
decl_stmt|;
while|while
condition|(
name|queue
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|ReaderAggregator
name|agg
init|=
name|queue
operator|.
name|top
argument_list|()
decl_stmt|;
name|BytesRef
name|value
init|=
name|agg
operator|.
name|copyCurrent
argument_list|()
decl_stmt|;
comment|// we need to makeSafe it, since we end up pushing it... (can we work around that?)
name|int
name|count
init|=
literal|0
decl_stmt|;
do|do
block|{
name|count
operator|+=
name|agg
operator|.
name|counts
operator|.
name|get
argument_list|(
name|agg
operator|.
name|position
argument_list|)
expr_stmt|;
if|if
condition|(
name|agg
operator|.
name|nextPosition
argument_list|()
condition|)
block|{
name|agg
operator|=
name|queue
operator|.
name|updateTop
argument_list|()
expr_stmt|;
block|}
else|else
block|{
comment|// we are done with this reader
name|queue
operator|.
name|pop
argument_list|()
expr_stmt|;
name|agg
operator|=
name|queue
operator|.
name|top
argument_list|()
expr_stmt|;
block|}
block|}
do|while
condition|(
name|agg
operator|!=
literal|null
operator|&&
name|value
operator|.
name|equals
argument_list|(
name|agg
operator|.
name|current
argument_list|)
condition|)
do|;
if|if
condition|(
name|count
operator|>
name|minCount
condition|)
block|{
if|if
condition|(
name|excluded
operator|!=
literal|null
operator|&&
name|excluded
operator|.
name|contains
argument_list|(
name|value
argument_list|)
condition|)
block|{
continue|continue;
block|}
if|if
condition|(
name|matcher
operator|!=
literal|null
condition|)
block|{
name|UnicodeUtil
operator|.
name|UTF8toUTF16
argument_list|(
name|value
argument_list|,
name|spare
argument_list|)
expr_stmt|;
assert|assert
name|spare
operator|.
name|toString
argument_list|()
operator|.
name|equals
argument_list|(
name|value
operator|.
name|utf8ToString
argument_list|()
argument_list|)
assert|;
if|if
condition|(
operator|!
name|matcher
operator|.
name|reset
argument_list|(
name|spare
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
continue|continue;
block|}
block|}
name|InternalStringTermsFacet
operator|.
name|TermEntry
name|entry
init|=
operator|new
name|InternalStringTermsFacet
operator|.
name|TermEntry
argument_list|(
name|value
argument_list|,
name|count
argument_list|)
decl_stmt|;
name|ordered
operator|.
name|add
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|InternalStringTermsFacet
argument_list|(
name|facetName
argument_list|,
name|comparatorType
argument_list|,
name|size
argument_list|,
name|ordered
argument_list|,
name|missing
argument_list|,
name|total
argument_list|)
return|;
block|}
DECL|class|Collector
class|class
name|Collector
extends|extends
name|FacetExecutor
operator|.
name|Collector
block|{
DECL|field|missing
specifier|private
name|long
name|missing
decl_stmt|;
DECL|field|total
specifier|private
name|long
name|total
decl_stmt|;
DECL|field|values
specifier|private
name|BytesValues
operator|.
name|WithOrdinals
name|values
decl_stmt|;
DECL|field|current
specifier|private
name|ReaderAggregator
name|current
decl_stmt|;
DECL|field|ordinals
specifier|private
name|Ordinals
operator|.
name|Docs
name|ordinals
decl_stmt|;
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
name|missing
operator|+=
name|current
operator|.
name|counts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|total
operator|+=
name|current
operator|.
name|total
operator|-
name|current
operator|.
name|counts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
if|if
condition|(
name|current
operator|.
name|values
operator|.
name|ordinals
argument_list|()
operator|.
name|getNumOrds
argument_list|()
operator|>
literal|0
condition|)
block|{
name|aggregators
operator|.
name|add
argument_list|(
name|current
argument_list|)
expr_stmt|;
block|}
block|}
name|values
operator|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getBytesValues
argument_list|()
expr_stmt|;
name|current
operator|=
operator|new
name|ReaderAggregator
argument_list|(
name|values
argument_list|,
name|ordinalsCacheAbove
argument_list|,
name|cacheRecycler
argument_list|)
expr_stmt|;
name|ordinals
operator|=
name|values
operator|.
name|ordinals
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|collect
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|int
name|length
init|=
name|ordinals
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
decl_stmt|;
name|int
name|missing
init|=
literal|1
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
name|length
condition|;
name|i
operator|++
control|)
block|{
name|current
operator|.
name|onOrdinal
argument_list|(
name|doc
argument_list|,
name|ordinals
operator|.
name|nextOrd
argument_list|()
argument_list|)
expr_stmt|;
name|missing
operator|=
literal|0
expr_stmt|;
block|}
name|current
operator|.
name|incrementMissing
argument_list|(
name|missing
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|postCollection
specifier|public
name|void
name|postCollection
parameter_list|()
block|{
if|if
condition|(
name|current
operator|!=
literal|null
condition|)
block|{
name|missing
operator|+=
name|current
operator|.
name|counts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|total
operator|+=
name|current
operator|.
name|total
operator|-
name|current
operator|.
name|counts
operator|.
name|get
argument_list|(
literal|0
argument_list|)
expr_stmt|;
comment|// if we have values for this one, add it
if|if
condition|(
name|current
operator|.
name|values
operator|.
name|ordinals
argument_list|()
operator|.
name|getNumOrds
argument_list|()
operator|>
literal|0
condition|)
block|{
name|aggregators
operator|.
name|add
argument_list|(
name|current
argument_list|)
expr_stmt|;
block|}
name|current
operator|=
literal|null
expr_stmt|;
block|}
name|TermsStringOrdinalsFacetExecutor
operator|.
name|this
operator|.
name|missing
operator|=
name|missing
expr_stmt|;
name|TermsStringOrdinalsFacetExecutor
operator|.
name|this
operator|.
name|total
operator|=
name|total
expr_stmt|;
block|}
block|}
DECL|class|ReaderAggregator
specifier|public
specifier|static
specifier|final
class|class
name|ReaderAggregator
block|{
DECL|field|maxOrd
specifier|private
specifier|final
name|long
name|maxOrd
decl_stmt|;
DECL|field|values
specifier|final
name|BytesValues
operator|.
name|WithOrdinals
name|values
decl_stmt|;
DECL|field|counts
specifier|final
name|IntArray
name|counts
decl_stmt|;
DECL|field|position
name|long
name|position
init|=
literal|0
decl_stmt|;
DECL|field|current
name|BytesRef
name|current
decl_stmt|;
DECL|field|total
name|int
name|total
decl_stmt|;
DECL|method|ReaderAggregator
specifier|public
name|ReaderAggregator
parameter_list|(
name|BytesValues
operator|.
name|WithOrdinals
name|values
parameter_list|,
name|int
name|ordinalsCacheLimit
parameter_list|,
name|CacheRecycler
name|cacheRecycler
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
name|values
expr_stmt|;
name|this
operator|.
name|maxOrd
operator|=
name|values
operator|.
name|ordinals
argument_list|()
operator|.
name|getMaxOrd
argument_list|()
expr_stmt|;
name|this
operator|.
name|counts
operator|=
name|IntArrays
operator|.
name|allocate
argument_list|(
name|maxOrd
argument_list|)
expr_stmt|;
block|}
DECL|method|onOrdinal
specifier|final
name|void
name|onOrdinal
parameter_list|(
name|int
name|docId
parameter_list|,
name|long
name|ordinal
parameter_list|)
block|{
name|counts
operator|.
name|increment
argument_list|(
name|ordinal
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|total
operator|++
expr_stmt|;
block|}
DECL|method|incrementMissing
specifier|final
name|void
name|incrementMissing
parameter_list|(
name|int
name|numMissing
parameter_list|)
block|{
name|counts
operator|.
name|increment
argument_list|(
literal|0
argument_list|,
name|numMissing
argument_list|)
expr_stmt|;
name|total
operator|+=
name|numMissing
expr_stmt|;
block|}
DECL|method|nextPosition
specifier|public
name|boolean
name|nextPosition
parameter_list|()
block|{
if|if
condition|(
operator|++
name|position
operator|>=
name|maxOrd
condition|)
block|{
return|return
literal|false
return|;
block|}
name|current
operator|=
name|values
operator|.
name|getValueByOrd
argument_list|(
name|position
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
DECL|method|copyCurrent
specifier|public
name|BytesRef
name|copyCurrent
parameter_list|()
block|{
return|return
name|values
operator|.
name|copyShared
argument_list|()
return|;
block|}
block|}
DECL|class|AggregatorPriorityQueue
specifier|public
specifier|static
class|class
name|AggregatorPriorityQueue
extends|extends
name|PriorityQueue
argument_list|<
name|ReaderAggregator
argument_list|>
block|{
DECL|method|AggregatorPriorityQueue
specifier|public
name|AggregatorPriorityQueue
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|super
argument_list|(
name|size
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|lessThan
specifier|protected
name|boolean
name|lessThan
parameter_list|(
name|ReaderAggregator
name|a
parameter_list|,
name|ReaderAggregator
name|b
parameter_list|)
block|{
return|return
name|a
operator|.
name|current
operator|.
name|compareTo
argument_list|(
name|b
operator|.
name|current
argument_list|)
operator|<
literal|0
return|;
block|}
block|}
block|}
end_class

end_unit

