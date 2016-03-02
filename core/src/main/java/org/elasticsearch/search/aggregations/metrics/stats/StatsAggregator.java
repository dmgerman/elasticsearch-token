begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.stats
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
name|stats
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
name|LeafReaderContext
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
name|BigArrays
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
name|DoubleArray
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
name|LongArray
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
name|SortedNumericDoubleValues
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
name|LeafBucketCollector
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
name|LeafBucketCollectorBase
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
name|NumericMetricsAggregator
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
name|pipeline
operator|.
name|PipelineAggregator
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
name|format
operator|.
name|ValueFormatter
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
name|List
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|StatsAggregator
specifier|public
class|class
name|StatsAggregator
extends|extends
name|NumericMetricsAggregator
operator|.
name|MultiValue
block|{
DECL|field|valuesSource
specifier|final
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
decl_stmt|;
DECL|field|formatter
specifier|final
name|ValueFormatter
name|formatter
decl_stmt|;
DECL|field|counts
name|LongArray
name|counts
decl_stmt|;
DECL|field|sums
name|DoubleArray
name|sums
decl_stmt|;
DECL|field|mins
name|DoubleArray
name|mins
decl_stmt|;
DECL|field|maxes
name|DoubleArray
name|maxes
decl_stmt|;
DECL|method|StatsAggregator
specifier|public
name|StatsAggregator
parameter_list|(
name|String
name|name
parameter_list|,
name|ValuesSource
operator|.
name|Numeric
name|valuesSource
parameter_list|,
name|ValueFormatter
name|formatter
parameter_list|,
name|AggregationContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|List
argument_list|<
name|PipelineAggregator
argument_list|>
name|pipelineAggregators
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|name
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
name|this
operator|.
name|valuesSource
operator|=
name|valuesSource
expr_stmt|;
if|if
condition|(
name|valuesSource
operator|!=
literal|null
condition|)
block|{
specifier|final
name|BigArrays
name|bigArrays
init|=
name|context
operator|.
name|bigArrays
argument_list|()
decl_stmt|;
name|counts
operator|=
name|bigArrays
operator|.
name|newLongArray
argument_list|(
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|sums
operator|=
name|bigArrays
operator|.
name|newDoubleArray
argument_list|(
literal|1
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|mins
operator|=
name|bigArrays
operator|.
name|newDoubleArray
argument_list|(
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|mins
operator|.
name|fill
argument_list|(
literal|0
argument_list|,
name|mins
operator|.
name|size
argument_list|()
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|)
expr_stmt|;
name|maxes
operator|=
name|bigArrays
operator|.
name|newDoubleArray
argument_list|(
literal|1
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|maxes
operator|.
name|fill
argument_list|(
literal|0
argument_list|,
name|maxes
operator|.
name|size
argument_list|()
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|formatter
operator|=
name|formatter
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|needsScores
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
name|valuesSource
operator|!=
literal|null
operator|&&
name|valuesSource
operator|.
name|needsScores
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getLeafCollector
specifier|public
name|LeafBucketCollector
name|getLeafCollector
parameter_list|(
name|LeafReaderContext
name|ctx
parameter_list|,
specifier|final
name|LeafBucketCollector
name|sub
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|valuesSource
operator|==
literal|null
condition|)
block|{
return|return
name|LeafBucketCollector
operator|.
name|NO_OP_COLLECTOR
return|;
block|}
specifier|final
name|BigArrays
name|bigArrays
init|=
name|context
operator|.
name|bigArrays
argument_list|()
decl_stmt|;
specifier|final
name|SortedNumericDoubleValues
name|values
init|=
name|valuesSource
operator|.
name|doubleValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
return|return
operator|new
name|LeafBucketCollectorBase
argument_list|(
name|sub
argument_list|,
name|values
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|,
name|long
name|bucket
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bucket
operator|>=
name|counts
operator|.
name|size
argument_list|()
condition|)
block|{
specifier|final
name|long
name|from
init|=
name|counts
operator|.
name|size
argument_list|()
decl_stmt|;
specifier|final
name|long
name|overSize
init|=
name|BigArrays
operator|.
name|overSize
argument_list|(
name|bucket
operator|+
literal|1
argument_list|)
decl_stmt|;
name|counts
operator|=
name|bigArrays
operator|.
name|resize
argument_list|(
name|counts
argument_list|,
name|overSize
argument_list|)
expr_stmt|;
name|sums
operator|=
name|bigArrays
operator|.
name|resize
argument_list|(
name|sums
argument_list|,
name|overSize
argument_list|)
expr_stmt|;
name|mins
operator|=
name|bigArrays
operator|.
name|resize
argument_list|(
name|mins
argument_list|,
name|overSize
argument_list|)
expr_stmt|;
name|maxes
operator|=
name|bigArrays
operator|.
name|resize
argument_list|(
name|maxes
argument_list|,
name|overSize
argument_list|)
expr_stmt|;
name|mins
operator|.
name|fill
argument_list|(
name|from
argument_list|,
name|overSize
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|)
expr_stmt|;
name|maxes
operator|.
name|fill
argument_list|(
name|from
argument_list|,
name|overSize
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|)
expr_stmt|;
block|}
name|values
operator|.
name|setDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
specifier|final
name|int
name|valuesCount
init|=
name|values
operator|.
name|count
argument_list|()
decl_stmt|;
name|counts
operator|.
name|increment
argument_list|(
name|bucket
argument_list|,
name|valuesCount
argument_list|)
expr_stmt|;
name|double
name|sum
init|=
literal|0
decl_stmt|;
name|double
name|min
init|=
name|mins
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
decl_stmt|;
name|double
name|max
init|=
name|maxes
operator|.
name|get
argument_list|(
name|bucket
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
name|valuesCount
condition|;
name|i
operator|++
control|)
block|{
name|double
name|value
init|=
name|values
operator|.
name|valueAt
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|sum
operator|+=
name|value
expr_stmt|;
name|min
operator|=
name|Math
operator|.
name|min
argument_list|(
name|min
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|max
operator|=
name|Math
operator|.
name|max
argument_list|(
name|max
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
name|sums
operator|.
name|increment
argument_list|(
name|bucket
argument_list|,
name|sum
argument_list|)
expr_stmt|;
name|mins
operator|.
name|set
argument_list|(
name|bucket
argument_list|,
name|min
argument_list|)
expr_stmt|;
name|maxes
operator|.
name|set
argument_list|(
name|bucket
argument_list|,
name|max
argument_list|)
expr_stmt|;
block|}
block|}
return|;
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
try|try
block|{
name|InternalStats
operator|.
name|Metrics
operator|.
name|resolve
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|iae
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
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
name|owningBucketOrd
parameter_list|)
block|{
switch|switch
condition|(
name|InternalStats
operator|.
name|Metrics
operator|.
name|resolve
argument_list|(
name|name
argument_list|)
condition|)
block|{
case|case
name|count
case|:
return|return
name|valuesSource
operator|==
literal|null
condition|?
literal|0
else|:
name|counts
operator|.
name|get
argument_list|(
name|owningBucketOrd
argument_list|)
return|;
case|case
name|sum
case|:
return|return
name|valuesSource
operator|==
literal|null
condition|?
literal|0
else|:
name|sums
operator|.
name|get
argument_list|(
name|owningBucketOrd
argument_list|)
return|;
case|case
name|min
case|:
return|return
name|valuesSource
operator|==
literal|null
condition|?
name|Double
operator|.
name|POSITIVE_INFINITY
else|:
name|mins
operator|.
name|get
argument_list|(
name|owningBucketOrd
argument_list|)
return|;
case|case
name|max
case|:
return|return
name|valuesSource
operator|==
literal|null
condition|?
name|Double
operator|.
name|NEGATIVE_INFINITY
else|:
name|maxes
operator|.
name|get
argument_list|(
name|owningBucketOrd
argument_list|)
return|;
case|case
name|avg
case|:
return|return
name|valuesSource
operator|==
literal|null
condition|?
name|Double
operator|.
name|NaN
else|:
name|sums
operator|.
name|get
argument_list|(
name|owningBucketOrd
argument_list|)
operator|/
name|counts
operator|.
name|get
argument_list|(
name|owningBucketOrd
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown value ["
operator|+
name|name
operator|+
literal|"] in common stats aggregation"
argument_list|)
throw|;
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
name|bucket
parameter_list|)
block|{
if|if
condition|(
name|valuesSource
operator|==
literal|null
operator|||
name|bucket
operator|>=
name|sums
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
name|buildEmptyAggregation
argument_list|()
return|;
block|}
return|return
operator|new
name|InternalStats
argument_list|(
name|name
argument_list|,
name|counts
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
argument_list|,
name|sums
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
argument_list|,
name|mins
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
argument_list|,
name|maxes
operator|.
name|get
argument_list|(
name|bucket
argument_list|)
argument_list|,
name|formatter
argument_list|,
name|pipelineAggregators
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
return|;
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
name|InternalStats
argument_list|(
name|name
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
argument_list|,
name|formatter
argument_list|,
name|pipelineAggregators
argument_list|()
argument_list|,
name|metaData
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doClose
specifier|public
name|void
name|doClose
parameter_list|()
block|{
name|Releasables
operator|.
name|close
argument_list|(
name|counts
argument_list|,
name|maxes
argument_list|,
name|mins
argument_list|,
name|sums
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

