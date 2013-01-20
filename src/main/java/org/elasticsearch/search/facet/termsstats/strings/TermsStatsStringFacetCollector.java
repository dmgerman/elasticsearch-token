begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.termsstats.strings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|termsstats
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
name|Lists
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
name|lucene
operator|.
name|HashedBytesRef
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
name|trove
operator|.
name|ExtTHashMap
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
name|HashedBytesValues
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
name|IndexNumericFieldData
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
name|termsstats
operator|.
name|TermsStatsFacet
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

begin_class
DECL|class|TermsStatsStringFacetCollector
specifier|public
class|class
name|TermsStatsStringFacetCollector
extends|extends
name|AbstractFacetCollector
block|{
DECL|field|comparatorType
specifier|private
specifier|final
name|TermsStatsFacet
operator|.
name|ComparatorType
name|comparatorType
decl_stmt|;
DECL|field|keyIndexFieldData
specifier|private
specifier|final
name|IndexFieldData
name|keyIndexFieldData
decl_stmt|;
DECL|field|valueIndexFieldData
specifier|private
specifier|final
name|IndexNumericFieldData
name|valueIndexFieldData
decl_stmt|;
DECL|field|script
specifier|private
specifier|final
name|SearchScript
name|script
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
DECL|field|aggregator
specifier|private
specifier|final
name|Aggregator
name|aggregator
decl_stmt|;
DECL|field|keyValues
specifier|private
name|HashedBytesValues
name|keyValues
decl_stmt|;
DECL|method|TermsStatsStringFacetCollector
specifier|public
name|TermsStatsStringFacetCollector
parameter_list|(
name|String
name|facetName
parameter_list|,
name|IndexFieldData
name|keyIndexFieldData
parameter_list|,
name|IndexNumericFieldData
name|valueIndexFieldData
parameter_list|,
name|SearchScript
name|valueScript
parameter_list|,
name|int
name|size
parameter_list|,
name|TermsStatsFacet
operator|.
name|ComparatorType
name|comparatorType
parameter_list|,
name|SearchContext
name|context
parameter_list|)
block|{
name|super
argument_list|(
name|facetName
argument_list|)
expr_stmt|;
name|this
operator|.
name|keyIndexFieldData
operator|=
name|keyIndexFieldData
expr_stmt|;
name|this
operator|.
name|valueIndexFieldData
operator|=
name|valueIndexFieldData
expr_stmt|;
name|this
operator|.
name|script
operator|=
name|valueScript
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
if|if
condition|(
name|script
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|aggregator
operator|=
operator|new
name|ScriptAggregator
argument_list|(
name|valueScript
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|aggregator
operator|=
operator|new
name|Aggregator
argument_list|()
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
name|keyValues
operator|=
name|keyIndexFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getHashedBytesValues
argument_list|()
expr_stmt|;
name|aggregator
operator|.
name|keyValues
operator|=
name|keyValues
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
else|else
block|{
name|aggregator
operator|.
name|valueValues
operator|=
name|valueIndexFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getDoubleValues
argument_list|()
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
name|keyValues
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
if|if
condition|(
name|aggregator
operator|.
name|entries
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
operator|new
name|InternalTermsStatsStringFacet
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
name|InternalTermsStatsStringFacet
operator|.
name|StringEntry
operator|>
name|of
argument_list|()
argument_list|,
name|aggregator
operator|.
name|missing
argument_list|)
return|;
block|}
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
comment|// all terms
comment|// all terms, just return the collection, we will sort it on the way back
return|return
operator|new
name|InternalTermsStatsStringFacet
argument_list|(
name|facetName
argument_list|,
name|comparatorType
argument_list|,
literal|0
comment|/* indicates all terms*/
argument_list|,
name|aggregator
operator|.
name|entries
operator|.
name|values
argument_list|()
argument_list|,
name|aggregator
operator|.
name|missing
argument_list|)
return|;
block|}
name|Object
index|[]
name|values
init|=
name|aggregator
operator|.
name|entries
operator|.
name|internalValues
argument_list|()
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|values
argument_list|,
operator|(
name|Comparator
operator|)
name|comparatorType
operator|.
name|comparator
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|InternalTermsStatsStringFacet
operator|.
name|StringEntry
argument_list|>
name|ordered
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|int
name|limit
init|=
name|size
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
name|limit
condition|;
name|i
operator|++
control|)
block|{
name|InternalTermsStatsStringFacet
operator|.
name|StringEntry
name|value
init|=
operator|(
name|InternalTermsStatsStringFacet
operator|.
name|StringEntry
operator|)
name|values
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
break|break;
block|}
name|ordered
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|CacheRecycler
operator|.
name|pushHashMap
argument_list|(
name|aggregator
operator|.
name|entries
argument_list|)
expr_stmt|;
comment|// fine to push here, we are done with it
return|return
operator|new
name|InternalTermsStatsStringFacet
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
argument_list|)
return|;
block|}
DECL|class|Aggregator
specifier|public
specifier|static
class|class
name|Aggregator
implements|implements
name|HashedBytesValues
operator|.
name|ValueInDocProc
block|{
DECL|field|entries
specifier|final
name|ExtTHashMap
argument_list|<
name|HashedBytesRef
argument_list|,
name|InternalTermsStatsStringFacet
operator|.
name|StringEntry
argument_list|>
name|entries
init|=
name|CacheRecycler
operator|.
name|popHashMap
argument_list|()
decl_stmt|;
DECL|field|missing
name|int
name|missing
init|=
literal|0
decl_stmt|;
DECL|field|keyValues
name|HashedBytesValues
name|keyValues
decl_stmt|;
DECL|field|valueValues
name|DoubleValues
name|valueValues
decl_stmt|;
DECL|field|valueAggregator
name|ValueAggregator
name|valueAggregator
init|=
operator|new
name|ValueAggregator
argument_list|()
decl_stmt|;
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
name|HashedBytesRef
name|value
parameter_list|)
block|{
name|InternalTermsStatsStringFacet
operator|.
name|StringEntry
name|stringEntry
init|=
name|entries
operator|.
name|get
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|stringEntry
operator|==
literal|null
condition|)
block|{
name|value
operator|=
name|keyValues
operator|.
name|makeSafe
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|stringEntry
operator|=
operator|new
name|InternalTermsStatsStringFacet
operator|.
name|StringEntry
argument_list|(
name|value
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
expr_stmt|;
name|entries
operator|.
name|put
argument_list|(
name|value
argument_list|,
name|stringEntry
argument_list|)
expr_stmt|;
block|}
name|stringEntry
operator|.
name|count
operator|++
expr_stmt|;
name|valueAggregator
operator|.
name|stringEntry
operator|=
name|stringEntry
expr_stmt|;
name|valueValues
operator|.
name|forEachValueInDoc
argument_list|(
name|docId
argument_list|,
name|valueAggregator
argument_list|)
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
DECL|class|ValueAggregator
specifier|public
specifier|static
class|class
name|ValueAggregator
implements|implements
name|DoubleValues
operator|.
name|ValueInDocProc
block|{
DECL|field|stringEntry
name|InternalTermsStatsStringFacet
operator|.
name|StringEntry
name|stringEntry
decl_stmt|;
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
block|{             }
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
name|value
operator|<
name|stringEntry
operator|.
name|min
condition|)
block|{
name|stringEntry
operator|.
name|min
operator|=
name|value
expr_stmt|;
block|}
if|if
condition|(
name|value
operator|>
name|stringEntry
operator|.
name|max
condition|)
block|{
name|stringEntry
operator|.
name|max
operator|=
name|value
expr_stmt|;
block|}
name|stringEntry
operator|.
name|total
operator|+=
name|value
expr_stmt|;
name|stringEntry
operator|.
name|totalCount
operator|++
expr_stmt|;
block|}
block|}
block|}
DECL|class|ScriptAggregator
specifier|public
specifier|static
class|class
name|ScriptAggregator
extends|extends
name|Aggregator
block|{
DECL|field|script
specifier|private
specifier|final
name|SearchScript
name|script
decl_stmt|;
DECL|method|ScriptAggregator
specifier|public
name|ScriptAggregator
parameter_list|(
name|SearchScript
name|script
parameter_list|)
block|{
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
name|HashedBytesRef
name|value
parameter_list|)
block|{
name|InternalTermsStatsStringFacet
operator|.
name|StringEntry
name|stringEntry
init|=
name|entries
operator|.
name|get
argument_list|(
name|value
argument_list|)
decl_stmt|;
if|if
condition|(
name|stringEntry
operator|==
literal|null
condition|)
block|{
name|value
operator|=
name|keyValues
operator|.
name|makeSafe
argument_list|(
name|value
argument_list|)
expr_stmt|;
name|stringEntry
operator|=
operator|new
name|InternalTermsStatsStringFacet
operator|.
name|StringEntry
argument_list|(
name|value
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
expr_stmt|;
name|entries
operator|.
name|put
argument_list|(
name|value
argument_list|,
name|stringEntry
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|stringEntry
operator|.
name|count
operator|++
expr_stmt|;
block|}
name|script
operator|.
name|setNextDocId
argument_list|(
name|docId
argument_list|)
expr_stmt|;
name|double
name|valueValue
init|=
name|script
operator|.
name|runAsDouble
argument_list|()
decl_stmt|;
if|if
condition|(
name|valueValue
operator|<
name|stringEntry
operator|.
name|min
condition|)
block|{
name|stringEntry
operator|.
name|min
operator|=
name|valueValue
expr_stmt|;
block|}
if|if
condition|(
name|valueValue
operator|>
name|stringEntry
operator|.
name|max
condition|)
block|{
name|stringEntry
operator|.
name|max
operator|=
name|valueValue
expr_stmt|;
block|}
name|stringEntry
operator|.
name|total
operator|+=
name|valueValue
expr_stmt|;
name|stringEntry
operator|.
name|totalCount
operator|++
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

