begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics
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
name|metrics
operator|.
name|percentiles
operator|.
name|PercentilesAggregationBuilder
import|;
end_import

begin_class
DECL|class|PercentilesTests
specifier|public
class|class
name|PercentilesTests
extends|extends
name|BaseAggregationTestCase
argument_list|<
name|PercentilesAggregationBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestAggregatorBuilder
specifier|protected
name|PercentilesAggregationBuilder
name|createTestAggregatorBuilder
parameter_list|()
block|{
name|PercentilesAggregationBuilder
name|factory
init|=
operator|new
name|PercentilesAggregationBuilder
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
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
name|int
name|percentsSize
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
name|percentsSize
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
name|percentsSize
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
name|randomDouble
argument_list|()
operator|*
literal|100
expr_stmt|;
block|}
name|factory
operator|.
name|percentiles
argument_list|(
name|percents
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
name|numberOfSignificantValueDigits
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|5
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
name|compression
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|50000
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|field
init|=
name|randomNumericField
argument_list|()
decl_stmt|;
name|int
name|randomFieldBranch
init|=
name|randomInt
argument_list|(
literal|3
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|randomFieldBranch
condition|)
block|{
case|case
literal|0
case|:
name|factory
operator|.
name|field
argument_list|(
name|field
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|factory
operator|.
name|field
argument_list|(
name|field
argument_list|)
expr_stmt|;
name|factory
operator|.
name|script
argument_list|(
operator|new
name|Script
argument_list|(
literal|"_value + 1"
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|factory
operator|.
name|script
argument_list|(
operator|new
name|Script
argument_list|(
literal|"doc["
operator|+
name|field
operator|+
literal|"] + 1"
argument_list|)
argument_list|)
expr_stmt|;
break|break;
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
literal|"MISSING"
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
name|format
argument_list|(
literal|"###.00"
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

