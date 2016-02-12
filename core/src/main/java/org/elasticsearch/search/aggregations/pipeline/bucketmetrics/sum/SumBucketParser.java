begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.bucketmetrics.sum
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
operator|.
name|bucketmetrics
operator|.
name|sum
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
name|pipeline
operator|.
name|bucketmetrics
operator|.
name|BucketMetricsParser
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
DECL|class|SumBucketParser
specifier|public
class|class
name|SumBucketParser
extends|extends
name|BucketMetricsParser
block|{
annotation|@
name|Override
DECL|method|type
specifier|public
name|String
name|type
parameter_list|()
block|{
return|return
name|SumBucketPipelineAggregator
operator|.
name|TYPE
operator|.
name|name
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|buildFactory
specifier|protected
name|SumBucketPipelineAggregator
operator|.
name|SumBucketPipelineAggregatorBuilder
name|buildFactory
parameter_list|(
name|String
name|pipelineAggregatorName
parameter_list|,
name|String
name|bucketsPath
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|unparsedParams
parameter_list|)
block|{
return|return
operator|new
name|SumBucketPipelineAggregator
operator|.
name|SumBucketPipelineAggregatorBuilder
argument_list|(
name|pipelineAggregatorName
argument_list|,
name|bucketsPath
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getFactoryPrototype
specifier|public
name|SumBucketPipelineAggregator
operator|.
name|SumBucketPipelineAggregatorBuilder
name|getFactoryPrototype
parameter_list|()
block|{
return|return
name|SumBucketPipelineAggregator
operator|.
name|SumBucketPipelineAggregatorBuilder
operator|.
name|PROTOTYPE
return|;
block|}
block|}
end_class

end_unit

