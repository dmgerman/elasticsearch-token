begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
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
name|BaseAggregationTestCase
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
name|range
operator|.
name|RangeAggregator
operator|.
name|Range
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
name|range
operator|.
name|RangeAggregationBuilder
import|;
end_import

begin_class
DECL|class|RangeTests
specifier|public
class|class
name|RangeTests
extends|extends
name|BaseAggregationTestCase
argument_list|<
name|RangeAggregationBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestAggregatorBuilder
specifier|protected
name|RangeAggregationBuilder
name|createTestAggregatorBuilder
parameter_list|()
block|{
name|int
name|numRanges
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|RangeAggregationBuilder
name|factory
init|=
operator|new
name|RangeAggregationBuilder
argument_list|(
literal|"foo"
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
name|numRanges
condition|;
name|i
operator|++
control|)
block|{
name|String
name|key
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|key
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
expr_stmt|;
block|}
name|double
name|from
init|=
name|randomBoolean
argument_list|()
condition|?
name|Double
operator|.
name|NEGATIVE_INFINITY
else|:
name|randomIntBetween
argument_list|(
name|Integer
operator|.
name|MIN_VALUE
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
operator|-
literal|1000
argument_list|)
decl_stmt|;
name|double
name|to
init|=
name|randomBoolean
argument_list|()
condition|?
name|Double
operator|.
name|POSITIVE_INFINITY
else|:
operator|(
name|Double
operator|.
name|isInfinite
argument_list|(
name|from
argument_list|)
condition|?
name|randomIntBetween
argument_list|(
name|Integer
operator|.
name|MIN_VALUE
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
else|:
name|randomIntBetween
argument_list|(
operator|(
name|int
operator|)
name|from
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|factory
operator|.
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|from
argument_list|,
name|to
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|fromAsStr
init|=
name|Double
operator|.
name|isInfinite
argument_list|(
name|from
argument_list|)
condition|?
literal|null
else|:
name|String
operator|.
name|valueOf
argument_list|(
name|from
argument_list|)
decl_stmt|;
name|String
name|toAsStr
init|=
name|Double
operator|.
name|isInfinite
argument_list|(
name|to
argument_list|)
condition|?
literal|null
else|:
name|String
operator|.
name|valueOf
argument_list|(
name|to
argument_list|)
decl_stmt|;
name|factory
operator|.
name|addRange
argument_list|(
operator|new
name|Range
argument_list|(
name|key
argument_list|,
name|fromAsStr
argument_list|,
name|toAsStr
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|factory
operator|.
name|field
argument_list|(
name|INT_FIELD_NAME
argument_list|)
expr_stmt|;
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
literal|"###.##"
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
name|keyed
argument_list|(
name|randomBoolean
argument_list|()
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
name|missing
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|10
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

