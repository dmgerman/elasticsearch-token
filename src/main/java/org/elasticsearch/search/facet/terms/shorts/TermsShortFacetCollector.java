begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.terms.shorts
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
name|shorts
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
name|TShortIntIterator
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
name|TShortIntHashMap
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
name|TShortHashSet
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
name|ElasticSearchIllegalArgumentException
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
name|index
operator|.
name|cache
operator|.
name|field
operator|.
name|data
operator|.
name|FieldDataCache
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
name|field
operator|.
name|data
operator|.
name|FieldDataType
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
name|field
operator|.
name|data
operator|.
name|shorts
operator|.
name|ShortFieldData
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
name|mapper
operator|.
name|MapperService
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
name|AbstractFacetCollector
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
name|Facet
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
name|FacetPhaseExecutionException
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
name|Map
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
DECL|class|TermsShortFacetCollector
specifier|public
class|class
name|TermsShortFacetCollector
extends|extends
name|AbstractFacetCollector
block|{
DECL|field|fieldDataCache
specifier|private
specifier|final
name|FieldDataCache
name|fieldDataCache
decl_stmt|;
DECL|field|indexFieldName
specifier|private
specifier|final
name|String
name|indexFieldName
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
DECL|field|numberOfShards
specifier|private
specifier|final
name|int
name|numberOfShards
decl_stmt|;
DECL|field|fieldDataType
specifier|private
specifier|final
name|FieldDataType
name|fieldDataType
decl_stmt|;
DECL|field|fieldData
specifier|private
name|ShortFieldData
name|fieldData
decl_stmt|;
DECL|field|aggregator
specifier|private
specifier|final
name|StaticAggregatorValueProc
name|aggregator
decl_stmt|;
DECL|field|script
specifier|private
specifier|final
name|SearchScript
name|script
decl_stmt|;
DECL|method|TermsShortFacetCollector
specifier|public
name|TermsShortFacetCollector
parameter_list|(
name|String
name|facetName
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|int
name|size
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
name|String
name|scriptLang
parameter_list|,
name|String
name|script
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
name|super
argument_list|(
name|facetName
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldDataCache
operator|=
name|context
operator|.
name|fieldDataCache
argument_list|()
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|comparatorType
operator|=
name|comparatorType
expr_stmt|;
name|this
operator|.
name|numberOfShards
operator|=
name|context
operator|.
name|numberOfShards
argument_list|()
expr_stmt|;
name|MapperService
operator|.
name|SmartNameFieldMappers
name|smartMappers
init|=
name|context
operator|.
name|smartFieldMappers
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|smartMappers
operator|==
literal|null
operator|||
operator|!
name|smartMappers
operator|.
name|hasMapper
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Field ["
operator|+
name|fieldName
operator|+
literal|"] doesn't have a type, can't run terms short facet collector on it"
argument_list|)
throw|;
block|}
comment|// add type filter if there is exact doc mapper associated with it
if|if
condition|(
name|smartMappers
operator|.
name|explicitTypeInNameWithDocMapper
argument_list|()
condition|)
block|{
name|setFilter
argument_list|(
name|context
operator|.
name|filterCache
argument_list|()
operator|.
name|cache
argument_list|(
name|smartMappers
operator|.
name|docMapper
argument_list|()
operator|.
name|typeFilter
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|smartMappers
operator|.
name|mapper
argument_list|()
operator|.
name|fieldDataType
argument_list|()
operator|!=
name|FieldDataType
operator|.
name|DefaultTypes
operator|.
name|SHORT
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"Field ["
operator|+
name|fieldName
operator|+
literal|"] is not of short type, can't run terms short facet collector on it"
argument_list|)
throw|;
block|}
name|this
operator|.
name|indexFieldName
operator|=
name|smartMappers
operator|.
name|mapper
argument_list|()
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
expr_stmt|;
name|this
operator|.
name|fieldDataType
operator|=
name|smartMappers
operator|.
name|mapper
argument_list|()
operator|.
name|fieldDataType
argument_list|()
expr_stmt|;
if|if
condition|(
name|script
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|script
operator|=
name|context
operator|.
name|scriptService
argument_list|()
operator|.
name|search
argument_list|(
name|context
operator|.
name|lookup
argument_list|()
argument_list|,
name|scriptLang
argument_list|,
name|script
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|script
operator|=
literal|null
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
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
name|CacheRecycler
operator|.
name|popShortIntMap
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
name|CacheRecycler
operator|.
name|popShortIntMap
argument_list|()
argument_list|,
name|excluded
argument_list|,
name|this
operator|.
name|script
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|allTerms
condition|)
block|{
try|try
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
name|ShortFieldData
name|fieldData
init|=
operator|(
name|ShortFieldData
operator|)
name|fieldDataCache
operator|.
name|cache
argument_list|(
name|fieldDataType
argument_list|,
name|readerContext
operator|.
name|reader
argument_list|()
argument_list|,
name|indexFieldName
argument_list|)
decl_stmt|;
name|fieldData
operator|.
name|forEachValue
argument_list|(
name|aggregator
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|FacetPhaseExecutionException
argument_list|(
name|facetName
argument_list|,
literal|"failed to load all terms"
argument_list|,
name|e
argument_list|)
throw|;
block|}
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
DECL|method|doSetNextReader
specifier|protected
name|void
name|doSetNextReader
parameter_list|(
name|AtomicReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|fieldData
operator|=
operator|(
name|ShortFieldData
operator|)
name|fieldDataCache
operator|.
name|cache
argument_list|(
name|fieldDataType
argument_list|,
name|context
operator|.
name|reader
argument_list|()
argument_list|,
name|indexFieldName
argument_list|)
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
DECL|method|doCollect
specifier|protected
name|void
name|doCollect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
name|fieldData
operator|.
name|forEachValueInDoc
argument_list|(
name|doc
argument_list|,
name|aggregator
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|facet
specifier|public
name|Facet
name|facet
parameter_list|()
block|{
name|TShortIntHashMap
name|facets
init|=
name|aggregator
operator|.
name|facets
argument_list|()
decl_stmt|;
if|if
condition|(
name|facets
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|CacheRecycler
operator|.
name|pushShortIntMap
argument_list|(
name|facets
argument_list|)
expr_stmt|;
return|return
operator|new
name|InternalShortTermsFacet
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
name|InternalShortTermsFacet
operator|.
name|ShortEntry
operator|>
name|of
argument_list|()
argument_list|,
name|aggregator
operator|.
name|missing
argument_list|()
argument_list|,
name|aggregator
operator|.
name|total
argument_list|()
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
name|size
argument_list|,
name|comparatorType
operator|.
name|comparator
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|TShortIntIterator
name|it
init|=
name|facets
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
name|InternalShortTermsFacet
operator|.
name|ShortEntry
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
name|InternalShortTermsFacet
operator|.
name|ShortEntry
index|[]
name|list
init|=
operator|new
name|InternalShortTermsFacet
operator|.
name|ShortEntry
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
name|InternalShortTermsFacet
operator|.
name|ShortEntry
operator|)
name|ordered
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
name|CacheRecycler
operator|.
name|pushShortIntMap
argument_list|(
name|facets
argument_list|)
expr_stmt|;
return|return
operator|new
name|InternalShortTermsFacet
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
name|aggregator
operator|.
name|missing
argument_list|()
argument_list|,
name|aggregator
operator|.
name|total
argument_list|()
argument_list|)
return|;
block|}
else|else
block|{
name|BoundedTreeSet
argument_list|<
name|InternalShortTermsFacet
operator|.
name|ShortEntry
argument_list|>
name|ordered
init|=
operator|new
name|BoundedTreeSet
argument_list|<
name|InternalShortTermsFacet
operator|.
name|ShortEntry
argument_list|>
argument_list|(
name|comparatorType
operator|.
name|comparator
argument_list|()
argument_list|,
name|size
argument_list|)
decl_stmt|;
for|for
control|(
name|TShortIntIterator
name|it
init|=
name|facets
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
name|InternalShortTermsFacet
operator|.
name|ShortEntry
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
name|CacheRecycler
operator|.
name|pushShortIntMap
argument_list|(
name|facets
argument_list|)
expr_stmt|;
return|return
operator|new
name|InternalShortTermsFacet
argument_list|(
name|facetName
argument_list|,
name|comparatorType
argument_list|,
name|size
argument_list|,
name|ordered
argument_list|,
name|aggregator
operator|.
name|missing
argument_list|()
argument_list|,
name|aggregator
operator|.
name|total
argument_list|()
argument_list|)
return|;
block|}
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
name|TShortHashSet
name|excluded
decl_stmt|;
DECL|method|AggregatorValueProc
specifier|public
name|AggregatorValueProc
parameter_list|(
name|TShortIntHashMap
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
name|TShortHashSet
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
name|Short
operator|.
name|parseShort
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
name|this
operator|.
name|script
operator|=
name|script
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
name|short
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
name|shortValue
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
implements|implements
name|ShortFieldData
operator|.
name|ValueInDocProc
implements|,
name|ShortFieldData
operator|.
name|ValueProc
block|{
DECL|field|facets
specifier|private
specifier|final
name|TShortIntHashMap
name|facets
decl_stmt|;
DECL|field|missing
specifier|private
name|int
name|missing
decl_stmt|;
DECL|field|total
specifier|private
name|int
name|total
decl_stmt|;
DECL|method|StaticAggregatorValueProc
specifier|public
name|StaticAggregatorValueProc
parameter_list|(
name|TShortIntHashMap
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
name|short
name|value
parameter_list|)
block|{
name|facets
operator|.
name|putIfAbsent
argument_list|(
name|value
argument_list|,
literal|0
argument_list|)
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
name|short
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
name|total
operator|++
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onMissing
specifier|public
name|void
name|onMissing
parameter_list|(
name|int
name|docId
parameter_list|)
block|{
name|missing
operator|++
expr_stmt|;
block|}
DECL|method|facets
specifier|public
specifier|final
name|TShortIntHashMap
name|facets
parameter_list|()
block|{
return|return
name|facets
return|;
block|}
DECL|method|missing
specifier|public
specifier|final
name|int
name|missing
parameter_list|()
block|{
return|return
name|this
operator|.
name|missing
return|;
block|}
DECL|method|total
specifier|public
specifier|final
name|int
name|total
parameter_list|()
block|{
return|return
name|this
operator|.
name|total
return|;
block|}
block|}
block|}
end_class

end_unit

