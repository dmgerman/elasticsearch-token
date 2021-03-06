begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|pipeline
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Script
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
name|bucketmetrics
operator|.
name|avg
operator|.
name|AvgBucketPipelineAggregationBuilder
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
name|bucketmetrics
operator|.
name|max
operator|.
name|MaxBucketPipelineAggregationBuilder
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
name|bucketmetrics
operator|.
name|min
operator|.
name|MinBucketPipelineAggregationBuilder
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
name|bucketmetrics
operator|.
name|percentile
operator|.
name|PercentilesBucketPipelineAggregationBuilder
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
name|bucketmetrics
operator|.
name|stats
operator|.
name|StatsBucketPipelineAggregationBuilder
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
name|bucketmetrics
operator|.
name|stats
operator|.
name|extended
operator|.
name|ExtendedStatsBucketPipelineAggregationBuilder
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
name|bucketmetrics
operator|.
name|sum
operator|.
name|SumBucketPipelineAggregationBuilder
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
name|bucketscript
operator|.
name|BucketScriptPipelineAggregationBuilder
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
name|bucketselector
operator|.
name|BucketSelectorPipelineAggregationBuilder
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
name|cumulativesum
operator|.
name|CumulativeSumPipelineAggregationBuilder
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
name|derivative
operator|.
name|DerivativePipelineAggregationBuilder
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
name|movavg
operator|.
name|MovAvgPipelineAggregationBuilder
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
name|serialdiff
operator|.
name|SerialDiffPipelineAggregationBuilder
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
DECL|class|PipelineAggregatorBuilders
specifier|public
specifier|final
class|class
name|PipelineAggregatorBuilders
block|{
DECL|method|PipelineAggregatorBuilders
specifier|private
name|PipelineAggregatorBuilders
parameter_list|()
block|{     }
DECL|method|derivative
specifier|public
specifier|static
name|DerivativePipelineAggregationBuilder
name|derivative
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
return|return
operator|new
name|DerivativePipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|bucketsPath
argument_list|)
return|;
block|}
DECL|method|maxBucket
specifier|public
specifier|static
name|MaxBucketPipelineAggregationBuilder
name|maxBucket
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
return|return
operator|new
name|MaxBucketPipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|bucketsPath
argument_list|)
return|;
block|}
DECL|method|minBucket
specifier|public
specifier|static
name|MinBucketPipelineAggregationBuilder
name|minBucket
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
return|return
operator|new
name|MinBucketPipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|bucketsPath
argument_list|)
return|;
block|}
DECL|method|avgBucket
specifier|public
specifier|static
name|AvgBucketPipelineAggregationBuilder
name|avgBucket
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
return|return
operator|new
name|AvgBucketPipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|bucketsPath
argument_list|)
return|;
block|}
DECL|method|sumBucket
specifier|public
specifier|static
name|SumBucketPipelineAggregationBuilder
name|sumBucket
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
return|return
operator|new
name|SumBucketPipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|bucketsPath
argument_list|)
return|;
block|}
DECL|method|statsBucket
specifier|public
specifier|static
name|StatsBucketPipelineAggregationBuilder
name|statsBucket
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
return|return
operator|new
name|StatsBucketPipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|bucketsPath
argument_list|)
return|;
block|}
DECL|method|extendedStatsBucket
specifier|public
specifier|static
name|ExtendedStatsBucketPipelineAggregationBuilder
name|extendedStatsBucket
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
return|return
operator|new
name|ExtendedStatsBucketPipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|bucketsPath
argument_list|)
return|;
block|}
DECL|method|percentilesBucket
specifier|public
specifier|static
name|PercentilesBucketPipelineAggregationBuilder
name|percentilesBucket
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
return|return
operator|new
name|PercentilesBucketPipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|bucketsPath
argument_list|)
return|;
block|}
DECL|method|movingAvg
specifier|public
specifier|static
name|MovAvgPipelineAggregationBuilder
name|movingAvg
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
return|return
operator|new
name|MovAvgPipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|bucketsPath
argument_list|)
return|;
block|}
DECL|method|bucketScript
specifier|public
specifier|static
name|BucketScriptPipelineAggregationBuilder
name|bucketScript
parameter_list|(
name|String
name|name
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|bucketsPathsMap
parameter_list|,
name|Script
name|script
parameter_list|)
block|{
return|return
operator|new
name|BucketScriptPipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|bucketsPathsMap
argument_list|,
name|script
argument_list|)
return|;
block|}
DECL|method|bucketScript
specifier|public
specifier|static
name|BucketScriptPipelineAggregationBuilder
name|bucketScript
parameter_list|(
name|String
name|name
parameter_list|,
name|Script
name|script
parameter_list|,
name|String
modifier|...
name|bucketsPaths
parameter_list|)
block|{
return|return
operator|new
name|BucketScriptPipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|script
argument_list|,
name|bucketsPaths
argument_list|)
return|;
block|}
DECL|method|bucketSelector
specifier|public
specifier|static
name|BucketSelectorPipelineAggregationBuilder
name|bucketSelector
parameter_list|(
name|String
name|name
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|bucketsPathsMap
parameter_list|,
name|Script
name|script
parameter_list|)
block|{
return|return
operator|new
name|BucketSelectorPipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|bucketsPathsMap
argument_list|,
name|script
argument_list|)
return|;
block|}
DECL|method|bucketSelector
specifier|public
specifier|static
name|BucketSelectorPipelineAggregationBuilder
name|bucketSelector
parameter_list|(
name|String
name|name
parameter_list|,
name|Script
name|script
parameter_list|,
name|String
modifier|...
name|bucketsPaths
parameter_list|)
block|{
return|return
operator|new
name|BucketSelectorPipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|script
argument_list|,
name|bucketsPaths
argument_list|)
return|;
block|}
DECL|method|cumulativeSum
specifier|public
specifier|static
name|CumulativeSumPipelineAggregationBuilder
name|cumulativeSum
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
return|return
operator|new
name|CumulativeSumPipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|bucketsPath
argument_list|)
return|;
block|}
DECL|method|diff
specifier|public
specifier|static
name|SerialDiffPipelineAggregationBuilder
name|diff
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
return|return
operator|new
name|SerialDiffPipelineAggregationBuilder
argument_list|(
name|name
argument_list|,
name|bucketsPath
argument_list|)
return|;
block|}
block|}
end_class

end_unit

