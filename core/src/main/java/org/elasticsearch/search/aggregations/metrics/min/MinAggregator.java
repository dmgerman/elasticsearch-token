begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.min
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
name|min
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
name|index
operator|.
name|fielddata
operator|.
name|NumericDoubleValues
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
name|MultiValueMode
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
DECL|class|MinAggregator
specifier|public
class|class
name|MinAggregator
extends|extends
name|NumericMetricsAggregator
operator|.
name|SingleValue
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
DECL|field|mins
name|DoubleArray
name|mins
decl_stmt|;
DECL|method|MinAggregator
specifier|public
name|MinAggregator
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
name|mins
operator|=
name|context
operator|.
name|bigArrays
argument_list|()
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
name|allValues
init|=
name|valuesSource
operator|.
name|doubleValues
argument_list|(
name|ctx
argument_list|)
decl_stmt|;
specifier|final
name|NumericDoubleValues
name|values
init|=
name|MultiValueMode
operator|.
name|MIN
operator|.
name|select
argument_list|(
name|allValues
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
argument_list|)
decl_stmt|;
return|return
operator|new
name|LeafBucketCollectorBase
argument_list|(
name|sub
argument_list|,
name|allValues
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
name|mins
operator|.
name|size
argument_list|()
condition|)
block|{
name|long
name|from
init|=
name|mins
operator|.
name|size
argument_list|()
decl_stmt|;
name|mins
operator|=
name|bigArrays
operator|.
name|grow
argument_list|(
name|mins
argument_list|,
name|bucket
operator|+
literal|1
argument_list|)
expr_stmt|;
name|mins
operator|.
name|fill
argument_list|(
name|from
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
block|}
specifier|final
name|double
name|value
init|=
name|values
operator|.
name|get
argument_list|(
name|doc
argument_list|)
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
name|mins
operator|.
name|set
argument_list|(
name|bucket
argument_list|,
name|min
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|metric
specifier|public
name|double
name|metric
parameter_list|(
name|long
name|owningBucketOrd
parameter_list|)
block|{
if|if
condition|(
name|valuesSource
operator|==
literal|null
operator|||
name|owningBucketOrd
operator|>=
name|mins
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
name|Double
operator|.
name|POSITIVE_INFINITY
return|;
block|}
return|return
name|mins
operator|.
name|get
argument_list|(
name|owningBucketOrd
argument_list|)
return|;
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
name|mins
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
name|InternalMin
argument_list|(
name|name
argument_list|,
name|mins
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
name|InternalMin
argument_list|(
name|name
argument_list|,
name|Double
operator|.
name|POSITIVE_INFINITY
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
name|mins
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

