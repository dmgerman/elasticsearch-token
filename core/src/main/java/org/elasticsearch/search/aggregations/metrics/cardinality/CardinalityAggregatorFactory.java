begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.cardinality
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
name|cardinality
package|;
end_package

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
name|bucket
operator|.
name|SingleBucketAggregator
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

begin_class
DECL|class|CardinalityAggregatorFactory
specifier|final
class|class
name|CardinalityAggregatorFactory
extends|extends
name|ValuesSourceAggregatorFactory
operator|.
name|LeafOnly
argument_list|<
name|ValuesSource
argument_list|>
block|{
DECL|field|precisionThreshold
specifier|private
specifier|final
name|long
name|precisionThreshold
decl_stmt|;
DECL|method|CardinalityAggregatorFactory
name|CardinalityAggregatorFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|ValuesSourceConfig
argument_list|<
name|ValuesSource
argument_list|>
name|config
parameter_list|,
name|long
name|precisionThreshold
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|InternalCardinality
operator|.
name|TYPE
operator|.
name|name
argument_list|()
argument_list|,
name|config
argument_list|)
expr_stmt|;
name|this
operator|.
name|precisionThreshold
operator|=
name|precisionThreshold
expr_stmt|;
block|}
DECL|method|precision
specifier|private
name|int
name|precision
parameter_list|(
name|Aggregator
name|parent
parameter_list|)
block|{
return|return
name|precisionThreshold
operator|<
literal|0
condition|?
name|defaultPrecision
argument_list|(
name|parent
argument_list|)
else|:
name|HyperLogLogPlusPlus
operator|.
name|precisionFromThreshold
argument_list|(
name|precisionThreshold
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createUnmapped
specifier|protected
name|Aggregator
name|createUnmapped
parameter_list|(
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
return|return
operator|new
name|CardinalityAggregator
argument_list|(
name|name
argument_list|,
literal|null
argument_list|,
name|precision
argument_list|(
name|parent
argument_list|)
argument_list|,
name|config
operator|.
name|formatter
argument_list|()
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doCreateInternal
specifier|protected
name|Aggregator
name|doCreateInternal
parameter_list|(
name|ValuesSource
name|valuesSource
parameter_list|,
name|AggregationContext
name|context
parameter_list|,
name|Aggregator
name|parent
parameter_list|,
name|boolean
name|collectsFromSingleBucket
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
return|return
operator|new
name|CardinalityAggregator
argument_list|(
name|name
argument_list|,
name|valuesSource
argument_list|,
name|precision
argument_list|(
name|parent
argument_list|)
argument_list|,
name|config
operator|.
name|formatter
argument_list|()
argument_list|,
name|context
argument_list|,
name|parent
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
comment|/*      * If one of the parent aggregators is a MULTI_BUCKET one, we might want to lower the precision      * because otherwise it might be memory-intensive. On the other hand, for top-level aggregators      * we try to focus on accuracy.      */
DECL|method|defaultPrecision
specifier|private
specifier|static
name|int
name|defaultPrecision
parameter_list|(
name|Aggregator
name|parent
parameter_list|)
block|{
name|int
name|precision
init|=
name|HyperLogLogPlusPlus
operator|.
name|DEFAULT_PRECISION
decl_stmt|;
while|while
condition|(
name|parent
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|parent
operator|instanceof
name|SingleBucketAggregator
operator|==
literal|false
condition|)
block|{
comment|// if the parent creates buckets, we substract 5 to the precision,
comment|// which will effectively divide the memory usage of each counter by 32
name|precision
operator|-=
literal|5
expr_stmt|;
block|}
name|parent
operator|=
name|parent
operator|.
name|parent
argument_list|()
expr_stmt|;
block|}
return|return
name|Math
operator|.
name|max
argument_list|(
name|precision
argument_list|,
name|HyperLogLogPlusPlus
operator|.
name|MIN_PRECISION
argument_list|)
return|;
block|}
block|}
end_class

end_unit

