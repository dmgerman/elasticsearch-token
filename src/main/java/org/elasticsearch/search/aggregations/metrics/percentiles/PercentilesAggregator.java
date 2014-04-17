begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.percentiles
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
operator|.
name|percentiles
package|;
end_package

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
name|common
operator|.
name|lease
operator|.
name|Releasables
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
name|ArrayUtils
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
name|ObjectArray
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
name|search
operator|.
name|aggregations
operator|.
name|Aggregator
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
name|aggregations
operator|.
name|InternalAggregation
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
name|aggregations
operator|.
name|metrics
operator|.
name|MetricsAggregator
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
name|aggregations
operator|.
name|metrics
operator|.
name|percentiles
operator|.
name|tdigest
operator|.
name|TDigestState
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
name|aggregations
operator|.
name|support
operator|.
name|AggregationContext
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
name|aggregations
operator|.
name|support
operator|.
name|ValuesSource
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
name|aggregations
operator|.
name|support
operator|.
name|ValuesSourceAggregatorFactory
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
name|aggregations
operator|.
name|support
operator|.
name|ValuesSourceConfig
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
comment|/**  *  */
end_comment

begin_class
DECL|class|PercentilesAggregator
specifier|public
class|class
name|PercentilesAggregator
extends|extends
name|MetricsAggregator
operator|.
name|MultiValue
block|{
DECL|method|indexOfPercent
specifier|private
specifier|static
name|int
name|indexOfPercent
parameter_list|(
name|double
index|[]
name|percents
parameter_list|,
name|double
name|percent
parameter_list|)
block|{
return|return
name|ArrayUtils
operator|.
name|binarySearch
argument_list|(
name|percents
argument_list|,
name|percent
argument_list|,
literal|0.001
argument_list|)
return|;
block|}
DECL|field|percents
specifier|private
specifier|final
name|double
index|[]
name|percents
decl_stmt|;
DECL|field|valuesSource
specifier|private
specifier|final
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
decl_stmt|;
DECL|field|values
specifier|private
name|DoubleValues
name|values
decl_stmt|;
DECL|field|states
specifier|private
name|ObjectArray
argument_list|<
name|TDigestState
argument_list|>
name|states
decl_stmt|;
DECL|field|compression
specifier|private
specifier|final
name|double
name|compression
decl_stmt|;
DECL|field|keyed
specifier|private
specifier|final
name|boolean
name|keyed
decl_stmt|;
DECL|method|PercentilesAggregator
specifier|public
name|PercentilesAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|estimatedBucketsCount
parameter_list|,
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
parameter_list|,
name|AggregationContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|double
index|[]
name|percents
parameter_list|,
name|double
name|compression
parameter_list|,
name|boolean
name|keyed
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|estimatedBucketsCount
argument_list|,
name|context
argument_list|,
name|parent
argument_list|)
expr_stmt|;
name|this
operator|.
name|valuesSource
operator|=
name|valuesSource
expr_stmt|;
name|this
operator|.
name|keyed
operator|=
name|keyed
expr_stmt|;
name|this
operator|.
name|states
operator|=
name|bigArrays
operator|.
name|newObjectArray
argument_list|(
name|estimatedBucketsCount
argument_list|)
expr_stmt|;
name|this
operator|.
name|percents
operator|=
name|percents
expr_stmt|;
name|this
operator|.
name|compression
operator|=
name|compression
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|shouldCollect
specifier|public
name|boolean
name|shouldCollect
parameter_list|()
block|{
return|return
name|valuesSource
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|AtomicReaderContext
name|reader
parameter_list|)
block|{
name|values
operator|=
name|valuesSource
operator|.
name|doubleValues
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
parameter_list|,
name|long
name|bucketOrd
parameter_list|)
throws|throws
name|IOException
block|{
name|states
operator|=
name|bigArrays
operator|.
name|grow
argument_list|(
name|states
argument_list|,
name|bucketOrd
operator|+
literal|1
argument_list|)
expr_stmt|;
name|TDigestState
name|state
init|=
name|states
operator|.
name|get
argument_list|(
name|bucketOrd
argument_list|)
decl_stmt|;
if|if
condition|(
name|state
operator|==
literal|null
condition|)
block|{
name|state
operator|=
operator|new
name|TDigestState
argument_list|(
name|compression
argument_list|)
expr_stmt|;
name|states
operator|.
name|set
argument_list|(
name|bucketOrd
argument_list|,
name|state
argument_list|)
expr_stmt|;
block|}
specifier|final
name|int
name|valueCount
init|=
name|values
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
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
name|valueCount
condition|;
name|i
operator|++
control|)
block|{
name|state
operator|.
name|add
argument_list|(
name|values
operator|.
name|nextValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|hasMetric
specifier|public
name|boolean
name|hasMetric
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|indexOfPercent
argument_list|(
name|percents
argument_list|,
name|Double
operator|.
name|parseDouble
argument_list|(
name|name
argument_list|)
argument_list|)
operator|>=
literal|0
return|;
block|}
DECL|method|getState
specifier|private
name|TDigestState
name|getState
parameter_list|(
name|long
name|bucketOrd
parameter_list|)
block|{
if|if
condition|(
name|bucketOrd
operator|>=
name|states
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
specifier|final
name|TDigestState
name|state
init|=
name|states
operator|.
name|get
argument_list|(
name|bucketOrd
argument_list|)
decl_stmt|;
return|return
name|state
return|;
block|}
annotation|@
name|Override
DECL|method|metric
specifier|public
name|double
name|metric
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|bucketOrd
parameter_list|)
block|{
name|TDigestState
name|state
init|=
name|getState
argument_list|(
name|bucketOrd
argument_list|)
decl_stmt|;
if|if
condition|(
name|state
operator|==
literal|null
condition|)
block|{
return|return
name|Double
operator|.
name|NaN
return|;
block|}
else|else
block|{
return|return
name|state
operator|.
name|quantile
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|name
argument_list|)
operator|/
literal|100
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|buildAggregation
specifier|public
name|InternalAggregation
name|buildAggregation
parameter_list|(
name|long
name|owningBucketOrdinal
parameter_list|)
block|{
name|TDigestState
name|state
init|=
name|getState
argument_list|(
name|owningBucketOrdinal
argument_list|)
decl_stmt|;
if|if
condition|(
name|state
operator|==
literal|null
condition|)
block|{
return|return
name|buildEmptyAggregation
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|new
name|InternalPercentiles
argument_list|(
name|name
argument_list|,
name|percents
argument_list|,
name|state
argument_list|,
name|keyed
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
DECL|method|buildEmptyAggregation
specifier|public
name|InternalAggregation
name|buildEmptyAggregation
parameter_list|()
block|{
return|return
operator|new
name|InternalPercentiles
argument_list|(
name|name
argument_list|,
name|percents
argument_list|,
operator|new
name|TDigestState
argument_list|(
name|compression
argument_list|)
argument_list|,
name|keyed
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|states
argument_list|)
expr_stmt|;
block|}
DECL|class|Factory
specifier|public
specifier|static
class|class
name|Factory
extends|extends
name|ValuesSourceAggregatorFactory
operator|.
name|LeafOnly
argument_list|<
name|ValuesSource
operator|.
name|Numeric
argument_list|>
block|{
DECL|field|percents
specifier|private
specifier|final
name|double
index|[]
name|percents
decl_stmt|;
DECL|field|compression
specifier|private
specifier|final
name|double
name|compression
decl_stmt|;
DECL|field|keyed
specifier|private
specifier|final
name|boolean
name|keyed
decl_stmt|;
DECL|method|Factory
specifier|public
name|Factory
parameter_list|(
name|String
name|name
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
operator|.
name|Numeric
argument_list|>
name|valuesSourceConfig
parameter_list|,
name|double
index|[]
name|percents
parameter_list|,
name|double
name|compression
parameter_list|,
name|boolean
name|keyed
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalPercentiles
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|,
name|valuesSourceConfig
argument_list|)
expr_stmt|;
name|this
operator|.
name|percents
operator|=
name|percents
expr_stmt|;
name|this
operator|.
name|compression
operator|=
name|compression
expr_stmt|;
name|this
operator|.
name|keyed
operator|=
name|keyed
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createUnmapped
specifier|protected
name|Aggregator
name|createUnmapped
parameter_list|(
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|)
block|{
return|return
operator|new
name|PercentilesAggregator
argument_list|(
name|name
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|percents
argument_list|,
name|compression
argument_list|,
name|keyed
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|protected
name|Aggregator
name|create
parameter_list|(
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
parameter_list|,
name|long
name|expectedBucketsCount
parameter_list|,
name|AggregationContext
name|aggregationContext
parameter_list|,
name|Aggregator
name|parent
parameter_list|)
block|{
return|return
operator|new
name|PercentilesAggregator
argument_list|(
name|name
argument_list|,
name|expectedBucketsCount
argument_list|,
name|valuesSource
argument_list|,
name|aggregationContext
argument_list|,
name|parent
argument_list|,
name|percents
argument_list|,
name|compression
argument_list|,
name|keyed
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

