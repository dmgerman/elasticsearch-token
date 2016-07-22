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
name|histogram
operator|.
name|HistogramAggregationBuilder
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
name|histogram
operator|.
name|Histogram
operator|.
name|Order
import|;
end_import

begin_class
DECL|class|HistogramTests
specifier|public
class|class
name|HistogramTests
extends|extends
name|BaseAggregationTestCase
argument_list|<
name|HistogramAggregationBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestAggregatorBuilder
specifier|protected
name|HistogramAggregationBuilder
name|createTestAggregatorBuilder
parameter_list|()
block|{
name|HistogramAggregationBuilder
name|factory
init|=
operator|new
name|HistogramAggregationBuilder
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|factory
operator|.
name|field
argument_list|(
name|INT_FIELD_NAME
argument_list|)
expr_stmt|;
name|factory
operator|.
name|interval
argument_list|(
name|randomDouble
argument_list|()
operator|*
literal|1000
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
name|extendedBounds
argument_list|(
name|randomDouble
argument_list|()
argument_list|,
name|randomDouble
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
name|minDocCount
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|100
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
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|factory
operator|.
name|offset
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|100000
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
name|int
name|branch
init|=
name|randomInt
argument_list|(
literal|5
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|branch
condition|)
block|{
case|case
literal|0
case|:
name|factory
operator|.
name|order
argument_list|(
name|Order
operator|.
name|COUNT_ASC
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|factory
operator|.
name|order
argument_list|(
name|Order
operator|.
name|COUNT_DESC
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|factory
operator|.
name|order
argument_list|(
name|Order
operator|.
name|KEY_ASC
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|factory
operator|.
name|order
argument_list|(
name|Order
operator|.
name|KEY_DESC
argument_list|)
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|factory
operator|.
name|order
argument_list|(
name|Order
operator|.
name|aggregation
argument_list|(
literal|"foo"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|5
case|:
name|factory
operator|.
name|order
argument_list|(
name|Order
operator|.
name|aggregation
argument_list|(
literal|"foo"
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
return|return
name|factory
return|;
block|}
block|}
end_class

end_unit

