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
name|common
operator|.
name|geo
operator|.
name|GeoDistance
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
name|geo
operator|.
name|GeoPoint
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
name|unit
operator|.
name|DistanceUnit
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
name|geodistance
operator|.
name|GeoDistanceAggregatorBuilder
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
name|geodistance
operator|.
name|GeoDistanceParser
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
name|test
operator|.
name|geo
operator|.
name|RandomShapeGenerator
import|;
end_import

begin_class
DECL|class|GeoDistanceRangeTests
specifier|public
class|class
name|GeoDistanceRangeTests
extends|extends
name|BaseAggregationTestCase
argument_list|<
name|GeoDistanceAggregatorBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestAggregatorBuilder
specifier|protected
name|GeoDistanceAggregatorBuilder
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
name|GeoPoint
name|origin
init|=
name|RandomShapeGenerator
operator|.
name|randomPoint
argument_list|(
name|getRandom
argument_list|()
argument_list|)
decl_stmt|;
name|GeoDistanceAggregatorBuilder
name|factory
init|=
operator|new
name|GeoDistanceAggregatorBuilder
argument_list|(
literal|"foo"
argument_list|,
name|origin
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
literal|0
else|:
name|randomIntBetween
argument_list|(
literal|0
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
name|compare
argument_list|(
name|from
argument_list|,
literal|0
argument_list|)
operator|==
literal|0
condition|?
name|randomIntBetween
argument_list|(
literal|0
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
name|factory
operator|.
name|field
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
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
literal|"0, 0"
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
name|unit
argument_list|(
name|randomFrom
argument_list|(
name|DistanceUnit
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
name|distanceType
argument_list|(
name|randomFrom
argument_list|(
name|GeoDistance
operator|.
name|values
argument_list|()
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

