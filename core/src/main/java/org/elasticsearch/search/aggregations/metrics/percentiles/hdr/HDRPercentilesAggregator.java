begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.percentiles.hdr
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
operator|.
name|hdr
package|;
end_package

begin_import
import|import
name|org
operator|.
name|HdrHistogram
operator|.
name|DoubleHistogram
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
operator|.
name|Numeric
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
DECL|class|HDRPercentilesAggregator
specifier|public
class|class
name|HDRPercentilesAggregator
extends|extends
name|AbstractHDRPercentilesAggregator
block|{
DECL|method|HDRPercentilesAggregator
specifier|public
name|HDRPercentilesAggregator
parameter_list|(
name|String
name|name
parameter_list|,
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
name|int
name|numberOfSignificantValueDigits
parameter_list|,
name|boolean
name|keyed
parameter_list|,
name|ValueFormatter
name|formatter
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
name|valuesSource
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|percents
argument_list|,
name|numberOfSignificantValueDigits
argument_list|,
name|keyed
argument_list|,
name|formatter
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
expr_stmt|;
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
name|DoubleHistogram
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
name|InternalHDRPercentiles
argument_list|(
name|name
argument_list|,
name|keys
argument_list|,
name|state
argument_list|,
name|keyed
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
name|DoubleHistogram
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
name|getValueAtPercentile
argument_list|(
name|Double
operator|.
name|parseDouble
argument_list|(
name|name
argument_list|)
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
name|DoubleHistogram
name|state
decl_stmt|;
name|state
operator|=
operator|new
name|DoubleHistogram
argument_list|(
name|numberOfSignificantValueDigits
argument_list|)
expr_stmt|;
name|state
operator|.
name|setAutoResize
argument_list|(
literal|true
argument_list|)
expr_stmt|;
return|return
operator|new
name|InternalHDRPercentiles
argument_list|(
name|name
argument_list|,
name|keys
argument_list|,
name|state
argument_list|,
name|keyed
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
block|}
end_class

end_unit

