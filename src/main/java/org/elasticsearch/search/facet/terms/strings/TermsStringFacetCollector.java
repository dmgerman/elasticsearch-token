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
name|TObjectIntIterator
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
name|TObjectIntHashMap
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
name|IndexReader
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
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadLocals
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
name|FieldData
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
name|ArrayDeque
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
name|Deque
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
DECL|class|TermsStringFacetCollector
specifier|public
class|class
name|TermsStringFacetCollector
extends|extends
name|AbstractFacetCollector
block|{
DECL|field|cache
specifier|static
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|Deque
argument_list|<
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
argument_list|>
name|cache
init|=
operator|new
name|ThreadLocal
argument_list|<
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|Deque
argument_list|<
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|Deque
argument_list|<
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|>
name|initialValue
parameter_list|()
block|{
return|return
operator|new
name|ThreadLocals
operator|.
name|CleanableValue
argument_list|<
name|Deque
argument_list|<
name|TObjectIntHashMap
argument_list|<
name|java
operator|.
name|lang
operator|.
name|String
argument_list|>
argument_list|>
argument_list|>
argument_list|(
operator|new
name|ArrayDeque
argument_list|<
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
argument_list|>
argument_list|()
argument_list|)
return|;
block|}
block|}
decl_stmt|;
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
name|FieldData
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
DECL|method|TermsStringFacetCollector
specifier|public
name|TermsStringFacetCollector
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
name|String
argument_list|>
name|excluded
parameter_list|,
name|Pattern
name|pattern
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
name|this
operator|.
name|indexFieldName
operator|=
name|fieldName
expr_stmt|;
name|this
operator|.
name|fieldDataType
operator|=
name|FieldDataType
operator|.
name|DefaultTypes
operator|.
name|STRING
expr_stmt|;
block|}
else|else
block|{
comment|// add type filter if there is exact doc mapper associated with it
if|if
condition|(
name|smartMappers
operator|.
name|hasDocMapper
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
block|}
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
name|excluded
operator|.
name|isEmpty
argument_list|()
operator|&&
name|pattern
operator|==
literal|null
operator|&&
name|this
operator|.
name|script
operator|==
literal|null
condition|)
block|{
name|aggregator
operator|=
operator|new
name|StaticAggregatorValueProc
argument_list|(
name|CacheRecycler
operator|.
expr|<
name|String
operator|>
name|popObjectIntMap
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
expr|<
name|String
operator|>
name|popObjectIntMap
argument_list|()
argument_list|,
name|excluded
argument_list|,
name|pattern
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
name|IndexReader
name|reader
range|:
name|context
operator|.
name|searcher
argument_list|()
operator|.
name|subReaders
argument_list|()
control|)
block|{
name|FieldData
name|fieldData
init|=
name|fieldDataCache
operator|.
name|cache
argument_list|(
name|fieldDataType
argument_list|,
name|reader
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
name|IndexReader
name|reader
parameter_list|,
name|int
name|docBase
parameter_list|)
throws|throws
name|IOException
block|{
name|fieldData
operator|=
name|fieldDataCache
operator|.
name|cache
argument_list|(
name|fieldDataType
argument_list|,
name|reader
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
name|reader
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
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
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
name|pushObjectIntMap
argument_list|(
name|facets
argument_list|)
expr_stmt|;
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
name|ImmutableList
operator|.
expr|<
name|InternalStringTermsFacet
operator|.
name|StringEntry
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
name|TObjectIntIterator
argument_list|<
name|String
argument_list|>
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
name|InternalStringTermsFacet
operator|.
name|StringEntry
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
name|InternalStringTermsFacet
operator|.
name|StringEntry
index|[]
name|list
init|=
operator|new
name|InternalStringTermsFacet
operator|.
name|StringEntry
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
operator|(
name|InternalStringTermsFacet
operator|.
name|StringEntry
operator|)
name|ordered
operator|.
name|pop
argument_list|()
operator|)
expr_stmt|;
block|}
name|CacheRecycler
operator|.
name|pushObjectIntMap
argument_list|(
name|facets
argument_list|)
expr_stmt|;
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
name|InternalStringTermsFacet
operator|.
name|StringEntry
argument_list|>
name|ordered
init|=
operator|new
name|BoundedTreeSet
argument_list|<
name|InternalStringTermsFacet
operator|.
name|StringEntry
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
name|TObjectIntIterator
argument_list|<
name|String
argument_list|>
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
name|InternalStringTermsFacet
operator|.
name|StringEntry
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
name|pushObjectIntMap
argument_list|(
name|facets
argument_list|)
expr_stmt|;
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
DECL|field|excluded
specifier|private
specifier|final
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|excluded
decl_stmt|;
DECL|field|matcher
specifier|private
specifier|final
name|Matcher
name|matcher
decl_stmt|;
DECL|field|script
specifier|private
specifier|final
name|SearchScript
name|script
decl_stmt|;
DECL|method|AggregatorValueProc
specifier|public
name|AggregatorValueProc
parameter_list|(
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
name|facets
parameter_list|,
name|ImmutableSet
argument_list|<
name|String
argument_list|>
name|excluded
parameter_list|,
name|Pattern
name|pattern
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
name|excluded
operator|=
name|excluded
expr_stmt|;
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
name|String
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
name|matcher
operator|!=
literal|null
operator|&&
operator|!
name|matcher
operator|.
name|reset
argument_list|(
name|value
argument_list|)
operator|.
name|matches
argument_list|()
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
name|scriptValue
operator|.
name|toString
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
name|FieldData
operator|.
name|StringValueInDocProc
implements|,
name|FieldData
operator|.
name|StringValueProc
block|{
DECL|field|facets
specifier|private
specifier|final
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
name|facets
decl_stmt|;
DECL|field|missing
specifier|private
name|int
name|missing
init|=
literal|0
decl_stmt|;
DECL|field|total
specifier|private
name|int
name|total
init|=
literal|0
decl_stmt|;
DECL|method|StaticAggregatorValueProc
specifier|public
name|StaticAggregatorValueProc
parameter_list|(
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
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
name|String
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
name|String
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
name|TObjectIntHashMap
argument_list|<
name|String
argument_list|>
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

