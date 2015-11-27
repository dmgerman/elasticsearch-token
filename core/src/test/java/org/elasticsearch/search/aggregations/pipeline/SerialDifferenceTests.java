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
name|search
operator|.
name|aggregations
operator|.
name|BasePipelineAggregationTestCase
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
name|BucketHelpers
operator|.
name|GapPolicy
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
name|SerialDiffPipelineAggregator
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
name|SerialDiffPipelineAggregator
operator|.
name|Factory
import|;
end_import

begin_class
DECL|class|SerialDifferenceTests
specifier|public
class|class
name|SerialDifferenceTests
extends|extends
name|BasePipelineAggregationTestCase
argument_list|<
name|SerialDiffPipelineAggregator
operator|.
name|Factory
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestAggregatorFactory
specifier|protected
name|Factory
name|createTestAggregatorFactory
parameter_list|()
block|{
name|String
name|name
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|20
argument_list|)
decl_stmt|;
name|String
index|[]
name|bucketsPaths
init|=
operator|new
name|String
index|[
literal|1
index|]
decl_stmt|;
name|bucketsPaths
index|[
literal|0
index|]
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|20
argument_list|)
expr_stmt|;
name|Factory
name|factory
init|=
operator|new
name|Factory
argument_list|(
name|name
argument_list|,
name|bucketsPaths
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|factory
operator|.
name|format
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|factory
operator|.
name|gapPolicy
argument_list|(
name|randomFrom
argument_list|(
name|GapPolicy
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|factory
operator|.
name|lag
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|1000
argument_list|)
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

