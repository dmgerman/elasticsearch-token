begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.terms.doubles
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
name|doubles
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
name|ImmutableList
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
name|gnu
operator|.
name|trove
operator|.
name|iterator
operator|.
name|TDoubleIntIterator
import|;
end_import

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|map
operator|.
name|hash
operator|.
name|TDoubleIntHashMap
import|;
end_import

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|set
operator|.
name|hash
operator|.
name|TDoubleHashSet
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
name|search
operator|.
name|Scorer
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
name|recycler
operator|.
name|Recycler
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
name|DoubleValues
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
name|IndexNumericFieldData
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
name|script
operator|.
name|SearchScript
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
name|DoubleFacetAggregatorBase
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
name|Arrays
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
DECL|class|TermsDoubleFacetExecutor
specifier|public
class|class
name|TermsDoubleFacetExecutor
extends|extends
name|FacetExecutor
block|{
DECL|field|indexFieldData
specifier|private
specifier|final
name|IndexNumericFieldData
name|indexFieldData
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
DECL|field|script
specifier|private
specifier|final
name|SearchScript
name|script
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
DECL|field|facets
specifier|final
name|Recycler
operator|.
name|V
argument_list|<
name|TDoubleIntHashMap
argument_list|>
name|facets
decl_stmt|;
DECL|field|missing
name|long
name|missing
decl_stmt|;
DECL|field|total
name|long
name|total
decl_stmt|;
DECL|method|TermsDoubleFacetExecutor
specifier|public
name|TermsDoubleFacetExecutor
parameter_list|(
name|IndexNumericFieldData
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
name|SearchScript
name|script
parameter_list|,
name|CacheRecycler
name|cacheRecycler
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
name|script
operator|=
name|script
expr_stmt|;
name|this
operator|.
name|excluded
operator|=
name|excluded
expr_stmt|;
name|this
operator|.
name|facets
operator|=
name|cacheRecycler
operator|.
name|doubleIntMap
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
if|if
condition|(
name|allTerms
condition|)
block|{
for|for
control|(
name|AtomicReaderContext
name|readerContext
range|:
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|getTopReaderContext
argument_list|()
operator|.
name|leaves
argument_list|()
control|)
block|{
name|int
name|maxDoc
init|=
name|readerContext
operator|.
name|reader
argument_list|()
operator|.
name|maxDoc
argument_list|()
decl_stmt|;
name|DoubleValues
name|values
init|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|readerContext
argument_list|)
operator|.
name|getDoubleValues
argument_list|()
decl_stmt|;
if|if
condition|(
name|values
operator|instanceof
name|DoubleValues
operator|.
name|WithOrdinals
condition|)
block|{
name|DoubleValues
operator|.
name|WithOrdinals
name|valuesWithOrds
init|=
operator|(
name|DoubleValues
operator|.
name|WithOrdinals
operator|)
name|values
decl_stmt|;
name|Ordinals
operator|.
name|Docs
name|ordinals
init|=
name|valuesWithOrds
operator|.
name|ordinals
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|ord
init|=
literal|1
init|;
name|ord
operator|<
name|ordinals
operator|.
name|getMaxOrd
argument_list|()
condition|;
name|ord
operator|++
control|)
block|{
name|facets
operator|.
name|v
argument_list|()
operator|.
name|putIfAbsent
argument_list|(
name|valuesWithOrds
operator|.
name|getValueByOrd
argument_list|(
name|ord
argument_list|)
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// Shouldn't be true, otherwise it is WithOrdinals... just to be sure...
if|if
condition|(
name|values
operator|.
name|isMultiValued
argument_list|()
condition|)
block|{
for|for
control|(
name|int
name|docId
init|=
literal|0
init|;
name|docId
operator|<
name|maxDoc
condition|;
name|docId
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|values
operator|.
name|hasValue
argument_list|(
name|docId
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|DoubleValues
operator|.
name|Iter
name|iter
init|=
name|values
operator|.
name|getIter
argument_list|(
name|docId
argument_list|)
decl_stmt|;
while|while
condition|(
name|iter
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|facets
operator|.
name|v
argument_list|()
operator|.
name|putIfAbsent
argument_list|(
name|iter
operator|.
name|next
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
for|for
control|(
name|int
name|docId
init|=
literal|0
init|;
name|docId
operator|<
name|maxDoc
condition|;
name|docId
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|values
operator|.
name|hasValue
argument_list|(
name|docId
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|double
name|value
init|=
name|values
operator|.
name|getValue
argument_list|(
name|docId
argument_list|)
decl_stmt|;
name|facets
operator|.
name|v
argument_list|()
operator|.
name|putIfAbsent
argument_list|(
name|value
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
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
if|if
condition|(
name|facets
operator|.
name|v
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|facets
operator|.
name|release
argument_list|()
expr_stmt|;
return|return
operator|new
name|InternalDoubleTermsFacet
argument_list|(
name|facetName
argument_list|,
name|comparatorType
argument_list|,
name|size
argument_list|,
name|ImmutableList
operator|.
expr|<
name|InternalDoubleTermsFacet
operator|.
name|DoubleEntry
operator|>
name|of
argument_list|()
argument_list|,
name|missing
argument_list|,
name|total
argument_list|)
return|;
block|}
else|else
block|{
if|if
condition|(
name|size
operator|<
name|EntryPriorityQueue
operator|.
name|LIMIT
condition|)
block|{
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
for|for
control|(
name|TDoubleIntIterator
name|it
init|=
name|facets
operator|.
name|v
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|it
operator|.
name|advance
argument_list|()
expr_stmt|;
name|ordered
operator|.
name|insertWithOverflow
argument_list|(
operator|new
name|InternalDoubleTermsFacet
operator|.
name|DoubleEntry
argument_list|(
name|it
operator|.
name|key
argument_list|()
argument_list|,
name|it
operator|.
name|value
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|InternalDoubleTermsFacet
operator|.
name|DoubleEntry
index|[]
name|list
init|=
operator|new
name|InternalDoubleTermsFacet
operator|.
name|DoubleEntry
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
name|InternalDoubleTermsFacet
operator|.
name|DoubleEntry
operator|)
name|ordered
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
name|facets
operator|.
name|release
argument_list|()
expr_stmt|;
return|return
operator|new
name|InternalDoubleTermsFacet
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
else|else
block|{
name|BoundedTreeSet
argument_list|<
name|InternalDoubleTermsFacet
operator|.
name|DoubleEntry
argument_list|>
name|ordered
init|=
operator|new
name|BoundedTreeSet
argument_list|<
name|InternalDoubleTermsFacet
operator|.
name|DoubleEntry
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
for|for
control|(
name|TDoubleIntIterator
name|it
init|=
name|facets
operator|.
name|v
argument_list|()
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|it
operator|.
name|advance
argument_list|()
expr_stmt|;
name|ordered
operator|.
name|add
argument_list|(
operator|new
name|InternalDoubleTermsFacet
operator|.
name|DoubleEntry
argument_list|(
name|it
operator|.
name|key
argument_list|()
argument_list|,
name|it
operator|.
name|value
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|facets
operator|.
name|release
argument_list|()
expr_stmt|;
return|return
operator|new
name|InternalDoubleTermsFacet
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
block|}
block|}
DECL|class|Collector
class|class
name|Collector
extends|extends
name|FacetExecutor
operator|.
name|Collector
block|{
DECL|field|aggregator
specifier|private
specifier|final
name|StaticAggregatorValueProc
name|aggregator
decl_stmt|;
DECL|field|values
specifier|private
name|DoubleValues
name|values
decl_stmt|;
DECL|method|Collector
specifier|public
name|Collector
parameter_list|()
block|{
if|if
condition|(
name|script
operator|==
literal|null
operator|&&
name|excluded
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|aggregator
operator|=
operator|new
name|StaticAggregatorValueProc
argument_list|(
name|facets
operator|.
name|v
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|aggregator
operator|=
operator|new
name|AggregatorValueProc
argument_list|(
name|facets
operator|.
name|v
argument_list|()
argument_list|,
name|excluded
argument_list|,
name|script
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|script
operator|!=
literal|null
condition|)
block|{
name|script
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
block|}
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
name|values
operator|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getDoubleValues
argument_list|()
expr_stmt|;
if|if
condition|(
name|script
operator|!=
literal|null
condition|)
block|{
name|script
operator|.
name|setNextReader
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
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
name|aggregator
operator|.
name|onDoc
argument_list|(
name|doc
argument_list|,
name|values
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
name|TermsDoubleFacetExecutor
operator|.
name|this
operator|.
name|missing
operator|=
name|aggregator
operator|.
name|missing
argument_list|()
expr_stmt|;
name|TermsDoubleFacetExecutor
operator|.
name|this
operator|.
name|total
operator|=
name|aggregator
operator|.
name|total
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|AggregatorValueProc
specifier|public
specifier|static
class|class
name|AggregatorValueProc
extends|extends
name|StaticAggregatorValueProc
block|{
DECL|field|script
specifier|private
specifier|final
name|SearchScript
name|script
decl_stmt|;
DECL|field|excluded
specifier|private
specifier|final
name|TDoubleHashSet
name|excluded
decl_stmt|;
DECL|method|AggregatorValueProc
specifier|public
name|AggregatorValueProc
parameter_list|(
name|TDoubleIntHashMap
name|facets
parameter_list|,
name|Set
argument_list|<
name|BytesRef
argument_list|>
name|excluded
parameter_list|,
name|SearchScript
name|script
parameter_list|)
block|{
name|super
argument_list|(
name|facets
argument_list|)
expr_stmt|;
name|this
operator|.
name|script
operator|=
name|script
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
operator|new
name|TDoubleHashSet
argument_list|(
name|excluded
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|BytesRef
name|s
range|:
name|excluded
control|)
block|{
name|this
operator|.
name|excluded
operator|.
name|add
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|s
operator|.
name|utf8ToString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|onValue
specifier|public
name|void
name|onValue
parameter_list|(
name|int
name|docId
parameter_list|,
name|double
name|value
parameter_list|)
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
return|return;
block|}
if|if
condition|(
name|script
operator|!=
literal|null
condition|)
block|{
name|script
operator|.
name|setNextDocId
argument_list|(
name|docId
argument_list|)
expr_stmt|;
name|script
operator|.
name|setNextVar
argument_list|(
literal|"term"
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|Object
name|scriptValue
init|=
name|script
operator|.
name|run
argument_list|()
decl_stmt|;
if|if
condition|(
name|scriptValue
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|scriptValue
operator|instanceof
name|Boolean
condition|)
block|{
if|if
condition|(
operator|!
operator|(
operator|(
name|Boolean
operator|)
name|scriptValue
operator|)
condition|)
block|{
return|return;
block|}
block|}
else|else
block|{
name|value
operator|=
operator|(
operator|(
name|Number
operator|)
name|scriptValue
operator|)
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
block|}
name|super
operator|.
name|onValue
argument_list|(
name|docId
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|StaticAggregatorValueProc
specifier|public
specifier|static
class|class
name|StaticAggregatorValueProc
extends|extends
name|DoubleFacetAggregatorBase
block|{
DECL|field|facets
specifier|private
specifier|final
name|TDoubleIntHashMap
name|facets
decl_stmt|;
DECL|method|StaticAggregatorValueProc
specifier|public
name|StaticAggregatorValueProc
parameter_list|(
name|TDoubleIntHashMap
name|facets
parameter_list|)
block|{
name|this
operator|.
name|facets
operator|=
name|facets
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onValue
specifier|public
name|void
name|onValue
parameter_list|(
name|int
name|docId
parameter_list|,
name|double
name|value
parameter_list|)
block|{
name|facets
operator|.
name|adjustOrPutValue
argument_list|(
name|value
argument_list|,
literal|1
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|facets
specifier|public
specifier|final
name|TDoubleIntHashMap
name|facets
parameter_list|()
block|{
return|return
name|facets
return|;
block|}
block|}
block|}
end_class

end_unit

