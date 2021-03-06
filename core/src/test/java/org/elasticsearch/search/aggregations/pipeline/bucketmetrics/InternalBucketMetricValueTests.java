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
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|InternalAggregationTestCase
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
DECL|class|InternalBucketMetricValueTests
specifier|public
class|class
name|InternalBucketMetricValueTests
extends|extends
name|InternalAggregationTestCase
argument_list|<
name|InternalBucketMetricValue
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestInstance
specifier|protected
name|InternalBucketMetricValue
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
name|double
name|value
init|=
name|frequently
argument_list|()
condition|?
name|randomDoubleBetween
argument_list|(
operator|-
literal|10000
argument_list|,
literal|100000
argument_list|,
literal|true
argument_list|)
else|:
name|randomFrom
argument_list|(
operator|new
name|Double
index|[]
block|{
name|Double
operator|.
name|NEGATIVE_INFINITY
block|,
name|Double
operator|.
name|POSITIVE_INFINITY
block|,
name|Double
operator|.
name|NaN
block|}
argument_list|)
decl_stmt|;
name|String
index|[]
name|keys
init|=
operator|new
name|String
index|[
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
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
name|keys
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|keys
index|[
name|i
index|]
operator|=
name|randomAlphaOfLength
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|InternalBucketMetricValue
argument_list|(
name|name
argument_list|,
name|keys
argument_list|,
name|value
argument_list|,
name|randomNumericDocValueFormat
argument_list|()
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|testReduceRandom
specifier|public
name|void
name|testReduceRandom
parameter_list|()
block|{
name|expectThrows
argument_list|(
name|UnsupportedOperationException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|createTestInstance
argument_list|(
literal|"name"
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|,
literal|null
argument_list|)
operator|.
name|reduce
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|assertReduced
specifier|protected
name|void
name|assertReduced
parameter_list|(
name|InternalBucketMetricValue
name|reduced
parameter_list|,
name|List
argument_list|<
name|InternalBucketMetricValue
argument_list|>
name|inputs
parameter_list|)
block|{
comment|// no test since reduce operation is unsupported
block|}
annotation|@
name|Override
DECL|method|instanceReader
specifier|protected
name|Reader
argument_list|<
name|InternalBucketMetricValue
argument_list|>
name|instanceReader
parameter_list|()
block|{
return|return
name|InternalBucketMetricValue
operator|::
operator|new
return|;
block|}
annotation|@
name|Override
DECL|method|assertFromXContent
specifier|protected
name|void
name|assertFromXContent
parameter_list|(
name|InternalBucketMetricValue
name|bucketMetricValue
parameter_list|,
name|ParsedAggregation
name|parsedAggregation
parameter_list|)
block|{
name|BucketMetricValue
name|parsed
init|=
operator|(
operator|(
name|BucketMetricValue
operator|)
name|parsedAggregation
operator|)
decl_stmt|;
name|assertArrayEquals
argument_list|(
name|bucketMetricValue
operator|.
name|keys
argument_list|()
argument_list|,
name|parsed
operator|.
name|keys
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|Double
operator|.
name|isInfinite
argument_list|(
name|bucketMetricValue
operator|.
name|value
argument_list|()
argument_list|)
operator|==
literal|false
condition|)
block|{
name|assertEquals
argument_list|(
name|bucketMetricValue
operator|.
name|value
argument_list|()
argument_list|,
name|parsed
operator|.
name|value
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|bucketMetricValue
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
else|else
block|{
comment|// we write Double.NEGATIVE_INFINITY and Double.POSITIVE_INFINITY to xContent as 'null', so we
comment|// cannot differentiate between them. Also we cannot recreate the exact String representation
name|assertEquals
argument_list|(
name|parsed
operator|.
name|value
argument_list|()
argument_list|,
name|Double
operator|.
name|NEGATIVE_INFINITY
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

