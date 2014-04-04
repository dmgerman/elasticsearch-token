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
name|LongLongOpenHashMap
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

begin_comment
comment|/**  * A date histogram facet collector that uses the same field as the key as well as the  * value.  */
end_comment

begin_class
DECL|class|CountDateHistogramFacetExecutor
specifier|public
class|class
name|CountDateHistogramFacetExecutor
extends|extends
name|FacetExecutor
block|{
DECL|field|tzRounding
specifier|private
specifier|final
name|TimeZoneRounding
name|tzRounding
decl_stmt|;
DECL|field|indexFieldData
specifier|private
specifier|final
name|IndexNumericFieldData
name|indexFieldData
decl_stmt|;
DECL|field|comparatorType
specifier|final
name|DateHistogramFacet
operator|.
name|ComparatorType
name|comparatorType
decl_stmt|;
DECL|field|counts
specifier|final
name|Recycler
operator|.
name|V
argument_list|<
name|LongLongOpenHashMap
argument_list|>
name|counts
decl_stmt|;
DECL|method|CountDateHistogramFacetExecutor
specifier|public
name|CountDateHistogramFacetExecutor
parameter_list|(
name|IndexNumericFieldData
name|indexFieldData
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
name|indexFieldData
operator|=
name|indexFieldData
expr_stmt|;
name|this
operator|.
name|tzRounding
operator|=
name|tzRounding
expr_stmt|;
name|this
operator|.
name|counts
operator|=
name|cacheRecycler
operator|.
name|longLongMap
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
name|InternalCountDateHistogramFacet
operator|.
name|CountEntry
index|[]
name|countEntries
init|=
operator|new
name|InternalCountDateHistogramFacet
operator|.
name|CountEntry
index|[
name|counts
operator|.
name|v
argument_list|()
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
specifier|final
name|boolean
index|[]
name|states
init|=
name|counts
operator|.
name|v
argument_list|()
operator|.
name|allocated
decl_stmt|;
specifier|final
name|long
index|[]
name|keys
init|=
name|counts
operator|.
name|v
argument_list|()
operator|.
name|keys
decl_stmt|;
specifier|final
name|long
index|[]
name|values
init|=
name|counts
operator|.
name|v
argument_list|()
operator|.
name|values
decl_stmt|;
name|int
name|entryIndex
init|=
literal|0
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
name|countEntries
index|[
name|entryIndex
operator|++
index|]
operator|=
operator|new
name|InternalCountDateHistogramFacet
operator|.
name|CountEntry
argument_list|(
name|keys
index|[
name|i
index|]
argument_list|,
name|values
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|counts
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
operator|new
name|InternalCountDateHistogramFacet
argument_list|(
name|facetName
argument_list|,
name|comparatorType
argument_list|,
name|countEntries
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
DECL|field|values
specifier|private
name|LongValues
name|values
decl_stmt|;
DECL|field|histoProc
specifier|private
specifier|final
name|DateHistogramProc
name|histoProc
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
name|counts
operator|.
name|v
argument_list|()
argument_list|,
name|tzRounding
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
name|values
operator|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|context
argument_list|)
operator|.
name|getLongValues
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
DECL|field|counts
specifier|private
specifier|final
name|LongLongOpenHashMap
name|counts
decl_stmt|;
DECL|field|tzRounding
specifier|private
specifier|final
name|TimeZoneRounding
name|tzRounding
decl_stmt|;
DECL|method|DateHistogramProc
specifier|public
name|DateHistogramProc
parameter_list|(
name|LongLongOpenHashMap
name|counts
parameter_list|,
name|TimeZoneRounding
name|tzRounding
parameter_list|)
block|{
name|this
operator|.
name|counts
operator|=
name|counts
expr_stmt|;
name|this
operator|.
name|tzRounding
operator|=
name|tzRounding
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
name|counts
operator|.
name|addTo
argument_list|(
name|tzRounding
operator|.
name|round
argument_list|(
name|value
argument_list|)
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|counts
specifier|public
name|LongLongOpenHashMap
name|counts
parameter_list|()
block|{
return|return
name|counts
return|;
block|}
block|}
block|}
end_class

end_unit

