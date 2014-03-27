begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.datehistogram
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|datehistogram
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|LongObjectOpenHashMap
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
name|common
operator|.
name|rounding
operator|.
name|TimeZoneRounding
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
name|LongValues
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
name|LongFacetAggregatorBase
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

begin_comment
comment|/**  * A histogram facet collector that uses different fields for the key and the value.  */
end_comment

begin_class
DECL|class|ValueDateHistogramFacetExecutor
specifier|public
class|class
name|ValueDateHistogramFacetExecutor
extends|extends
name|FacetExecutor
block|{
DECL|field|keyIndexFieldData
specifier|private
specifier|final
name|IndexNumericFieldData
name|keyIndexFieldData
decl_stmt|;
DECL|field|valueIndexFieldData
specifier|private
specifier|final
name|IndexNumericFieldData
name|valueIndexFieldData
decl_stmt|;
DECL|field|comparatorType
specifier|private
specifier|final
name|DateHistogramFacet
operator|.
name|ComparatorType
name|comparatorType
decl_stmt|;
DECL|field|tzRounding
specifier|final
name|TimeZoneRounding
name|tzRounding
decl_stmt|;
DECL|field|entries
specifier|final
name|Recycler
operator|.
name|V
argument_list|<
name|LongObjectOpenHashMap
argument_list|<
name|InternalFullDateHistogramFacet
operator|.
name|FullEntry
argument_list|>
argument_list|>
name|entries
decl_stmt|;
DECL|method|ValueDateHistogramFacetExecutor
specifier|public
name|ValueDateHistogramFacetExecutor
parameter_list|(
name|IndexNumericFieldData
name|keyIndexFieldData
parameter_list|,
name|IndexNumericFieldData
name|valueIndexFieldData
parameter_list|,
name|TimeZoneRounding
name|tzRounding
parameter_list|,
name|DateHistogramFacet
operator|.
name|ComparatorType
name|comparatorType
parameter_list|,
name|CacheRecycler
name|cacheRecycler
parameter_list|)
block|{
name|this
operator|.
name|comparatorType
operator|=
name|comparatorType
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
name|tzRounding
operator|=
name|tzRounding
expr_stmt|;
name|this
operator|.
name|entries
operator|=
name|cacheRecycler
operator|.
name|longObjectMap
argument_list|(
operator|-
literal|1
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
name|ArrayList
argument_list|<
name|InternalFullDateHistogramFacet
operator|.
name|FullEntry
argument_list|>
name|entries1
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|entries
operator|.
name|v
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|boolean
index|[]
name|states
init|=
name|entries
operator|.
name|v
argument_list|()
operator|.
name|allocated
decl_stmt|;
specifier|final
name|Object
index|[]
name|values
init|=
name|entries
operator|.
name|v
argument_list|()
operator|.
name|values
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
name|states
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|states
index|[
name|i
index|]
condition|)
block|{
name|InternalFullDateHistogramFacet
operator|.
name|FullEntry
name|value
init|=
operator|(
name|InternalFullDateHistogramFacet
operator|.
name|FullEntry
operator|)
name|values
index|[
name|i
index|]
decl_stmt|;
name|entries1
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
name|entries
operator|.
name|release
argument_list|()
expr_stmt|;
return|return
operator|new
name|InternalFullDateHistogramFacet
argument_list|(
name|facetName
argument_list|,
name|comparatorType
argument_list|,
name|entries1
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
DECL|field|histoProc
specifier|private
specifier|final
name|DateHistogramProc
name|histoProc
decl_stmt|;
DECL|field|keyValues
specifier|private
name|LongValues
name|keyValues
decl_stmt|;
DECL|method|Collector
specifier|public
name|Collector
parameter_list|()
block|{
name|this
operator|.
name|histoProc
operator|=
operator|new
name|DateHistogramProc
argument_list|(
name|tzRounding
argument_list|,
name|entries
operator|.
name|v
argument_list|()
argument_list|)
expr_stmt|;
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
name|keyValues
operator|=
name|keyIndexFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getLongValues
argument_list|()
expr_stmt|;
name|histoProc
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
name|histoProc
operator|.
name|onDoc
argument_list|(
name|doc
argument_list|,
name|keyValues
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
block|{         }
block|}
DECL|class|DateHistogramProc
specifier|public
specifier|static
class|class
name|DateHistogramProc
extends|extends
name|LongFacetAggregatorBase
block|{
DECL|field|entries
specifier|final
name|LongObjectOpenHashMap
argument_list|<
name|InternalFullDateHistogramFacet
operator|.
name|FullEntry
argument_list|>
name|entries
decl_stmt|;
DECL|field|tzRounding
specifier|private
specifier|final
name|TimeZoneRounding
name|tzRounding
decl_stmt|;
DECL|field|valueValues
name|DoubleValues
name|valueValues
decl_stmt|;
DECL|field|valueAggregator
specifier|final
name|ValueAggregator
name|valueAggregator
init|=
operator|new
name|ValueAggregator
argument_list|()
decl_stmt|;
DECL|method|DateHistogramProc
specifier|public
name|DateHistogramProc
parameter_list|(
name|TimeZoneRounding
name|tzRounding
parameter_list|,
name|LongObjectOpenHashMap
argument_list|<
name|InternalFullDateHistogramFacet
operator|.
name|FullEntry
argument_list|>
name|entries
parameter_list|)
block|{
name|this
operator|.
name|tzRounding
operator|=
name|tzRounding
expr_stmt|;
name|this
operator|.
name|entries
operator|=
name|entries
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
name|long
name|value
parameter_list|)
block|{
name|long
name|time
init|=
name|tzRounding
operator|.
name|round
argument_list|(
name|value
argument_list|)
decl_stmt|;
name|InternalFullDateHistogramFacet
operator|.
name|FullEntry
name|entry
init|=
name|entries
operator|.
name|get
argument_list|(
name|time
argument_list|)
decl_stmt|;
if|if
condition|(
name|entry
operator|==
literal|null
condition|)
block|{
name|entry
operator|=
operator|new
name|InternalFullDateHistogramFacet
operator|.
name|FullEntry
argument_list|(
name|time
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
argument_list|,
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|entries
operator|.
name|put
argument_list|(
name|time
argument_list|,
name|entry
argument_list|)
expr_stmt|;
block|}
name|entry
operator|.
name|count
operator|++
expr_stmt|;
name|valueAggregator
operator|.
name|entry
operator|=
name|entry
expr_stmt|;
name|valueAggregator
operator|.
name|onDoc
argument_list|(
name|docId
argument_list|,
name|valueValues
argument_list|)
expr_stmt|;
block|}
DECL|class|ValueAggregator
specifier|public
specifier|final
specifier|static
class|class
name|ValueAggregator
extends|extends
name|DoubleFacetAggregatorBase
block|{
DECL|field|entry
name|InternalFullDateHistogramFacet
operator|.
name|FullEntry
name|entry
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
name|double
name|value
parameter_list|)
block|{
name|entry
operator|.
name|totalCount
operator|++
expr_stmt|;
name|entry
operator|.
name|total
operator|+=
name|value
expr_stmt|;
if|if
condition|(
name|value
operator|<
name|entry
operator|.
name|min
condition|)
block|{
name|entry
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
name|entry
operator|.
name|max
condition|)
block|{
name|entry
operator|.
name|max
operator|=
name|value
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
end_class

end_unit

