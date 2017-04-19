begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.avg
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
name|avg
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
name|io
operator|.
name|stream
operator|.
name|Writeable
operator|.
name|Reader
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
name|DocValueFormat
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
name|InternalAggregationTestCase
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
name|ParsedAggregation
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
DECL|class|InternalAvgTests
specifier|public
class|class
name|InternalAvgTests
extends|extends
name|InternalAggregationTestCase
argument_list|<
name|InternalAvg
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestInstance
specifier|protected
name|InternalAvg
name|createTestInstance
parameter_list|(
name|String
name|name
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
block|{
name|DocValueFormat
name|formatter
init|=
name|randomNumericDocValueFormat
argument_list|()
decl_stmt|;
name|long
name|count
init|=
name|frequently
argument_list|()
condition|?
name|randomNonNegativeLong
argument_list|()
operator|%
literal|100000
else|:
literal|0
decl_stmt|;
return|return
operator|new
name|InternalAvg
argument_list|(
name|name
argument_list|,
name|randomDoubleBetween
argument_list|(
literal|0
argument_list|,
literal|100000
argument_list|,
literal|true
argument_list|)
argument_list|,
name|count
argument_list|,
name|formatter
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|instanceReader
specifier|protected
name|Reader
argument_list|<
name|InternalAvg
argument_list|>
name|instanceReader
parameter_list|()
block|{
return|return
name|InternalAvg
operator|::
operator|new
return|;
block|}
annotation|@
name|Override
DECL|method|assertReduced
specifier|protected
name|void
name|assertReduced
parameter_list|(
name|InternalAvg
name|reduced
parameter_list|,
name|List
argument_list|<
name|InternalAvg
argument_list|>
name|inputs
parameter_list|)
block|{
name|double
name|sum
init|=
literal|0
decl_stmt|;
name|long
name|counts
init|=
literal|0
decl_stmt|;
for|for
control|(
name|InternalAvg
name|in
range|:
name|inputs
control|)
block|{
name|sum
operator|+=
name|in
operator|.
name|getSum
argument_list|()
expr_stmt|;
name|counts
operator|+=
name|in
operator|.
name|getCount
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|counts
argument_list|,
name|reduced
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|sum
argument_list|,
name|reduced
operator|.
name|getSum
argument_list|()
argument_list|,
literal|0.0000001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|sum
operator|/
name|counts
argument_list|,
name|reduced
operator|.
name|value
argument_list|()
argument_list|,
literal|0.0000001
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|assertFromXContent
specifier|protected
name|void
name|assertFromXContent
parameter_list|(
name|InternalAvg
name|avg
parameter_list|,
name|ParsedAggregation
name|parsedAggregation
parameter_list|)
block|{
name|ParsedAvg
name|parsed
init|=
operator|(
operator|(
name|ParsedAvg
operator|)
name|parsedAggregation
operator|)
decl_stmt|;
name|assertEquals
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|parsed
operator|.
name|getValue
argument_list|()
argument_list|,
name|Double
operator|.
name|MIN_VALUE
argument_list|)
expr_stmt|;
comment|// we don't print out VALUE_AS_STRING for avg.getCount() == 0, so we cannot get the exact same value back
if|if
condition|(
name|avg
operator|.
name|getCount
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|assertEquals
argument_list|(
name|avg
operator|.
name|getValueAsString
argument_list|()
argument_list|,
name|parsed
operator|.
name|getValueAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

