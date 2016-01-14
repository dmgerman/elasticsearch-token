begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.bucketmetrics
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
name|percentile
operator|.
name|PercentilesBucketPipelineAggregator
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
name|PercentilesBucketPipelineAggregator
operator|.
name|Factory
import|;
end_import

begin_class
DECL|class|PercentilesBucketTests
specifier|public
class|class
name|PercentilesBucketTests
extends|extends
name|AbstractBucketMetricsTestCase
argument_list|<
name|PercentilesBucketPipelineAggregator
operator|.
name|Factory
argument_list|>
block|{
annotation|@
name|Override
DECL|method|doCreateTestAggregatorFactory
specifier|protected
name|Factory
name|doCreateTestAggregatorFactory
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
name|Factory
name|factory
init|=
operator|new
name|Factory
argument_list|(
name|name
argument_list|,
name|bucketsPath
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|int
name|numPercents
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
decl_stmt|;
name|double
index|[]
name|percents
init|=
operator|new
name|double
index|[
name|numPercents
index|]
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
name|numPercents
condition|;
name|i
operator|++
control|)
block|{
name|percents
index|[
name|i
index|]
operator|=
name|randomDoubleBetween
argument_list|(
literal|0.0
argument_list|,
literal|100.0
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
name|factory
operator|.
name|percents
argument_list|(
name|percents
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
block|}
end_class

end_unit

