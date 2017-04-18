begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.percentiles.tdigest
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
name|percentiles
operator|.
name|tdigest
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
DECL|class|InternalTDigestPercentilesTests
specifier|public
class|class
name|InternalTDigestPercentilesTests
extends|extends
name|InternalAggregationTestCase
argument_list|<
name|InternalTDigestPercentiles
argument_list|>
block|{
DECL|field|percents
specifier|private
specifier|final
name|double
index|[]
name|percents
init|=
name|randomPercents
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|createTestInstance
specifier|protected
name|InternalTDigestPercentiles
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
name|boolean
name|keyed
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|DocValueFormat
name|format
init|=
name|DocValueFormat
operator|.
name|RAW
decl_stmt|;
name|TDigestState
name|state
init|=
operator|new
name|TDigestState
argument_list|(
literal|100
argument_list|)
decl_stmt|;
name|int
name|numValues
init|=
name|randomInt
argument_list|(
literal|10
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
name|numValues
condition|;
operator|++
name|i
control|)
block|{
name|state
operator|.
name|add
argument_list|(
name|randomDouble
argument_list|()
operator|*
literal|100
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|state
operator|.
name|centroidCount
argument_list|()
argument_list|,
name|numValues
argument_list|)
expr_stmt|;
return|return
operator|new
name|InternalTDigestPercentiles
argument_list|(
name|name
argument_list|,
name|percents
argument_list|,
name|state
argument_list|,
name|keyed
argument_list|,
name|format
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|assertReduced
specifier|protected
name|void
name|assertReduced
parameter_list|(
name|InternalTDigestPercentiles
name|reduced
parameter_list|,
name|List
argument_list|<
name|InternalTDigestPercentiles
argument_list|>
name|inputs
parameter_list|)
block|{
specifier|final
name|TDigestState
name|expectedState
init|=
operator|new
name|TDigestState
argument_list|(
name|reduced
operator|.
name|state
operator|.
name|compression
argument_list|()
argument_list|)
decl_stmt|;
name|long
name|totalCount
init|=
literal|0
decl_stmt|;
for|for
control|(
name|InternalTDigestPercentiles
name|input
range|:
name|inputs
control|)
block|{
name|assertArrayEquals
argument_list|(
name|reduced
operator|.
name|keys
argument_list|,
name|input
operator|.
name|keys
argument_list|,
literal|0d
argument_list|)
expr_stmt|;
name|expectedState
operator|.
name|add
argument_list|(
name|input
operator|.
name|state
argument_list|)
expr_stmt|;
name|totalCount
operator|+=
name|input
operator|.
name|state
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|totalCount
argument_list|,
name|reduced
operator|.
name|state
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|totalCount
operator|>
literal|0
condition|)
block|{
name|assertEquals
argument_list|(
name|expectedState
operator|.
name|quantile
argument_list|(
literal|0
argument_list|)
argument_list|,
name|reduced
operator|.
name|state
operator|.
name|quantile
argument_list|(
literal|0
argument_list|)
argument_list|,
literal|0d
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedState
operator|.
name|quantile
argument_list|(
literal|1
argument_list|)
argument_list|,
name|reduced
operator|.
name|state
operator|.
name|quantile
argument_list|(
literal|1
argument_list|)
argument_list|,
literal|0d
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|instanceReader
specifier|protected
name|Writeable
operator|.
name|Reader
argument_list|<
name|InternalTDigestPercentiles
argument_list|>
name|instanceReader
parameter_list|()
block|{
return|return
name|InternalTDigestPercentiles
operator|::
operator|new
return|;
block|}
DECL|method|randomPercents
specifier|private
specifier|static
name|double
index|[]
name|randomPercents
parameter_list|()
block|{
name|List
argument_list|<
name|Double
argument_list|>
name|randomCdfValues
init|=
name|randomSubsetOf
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|7
argument_list|)
argument_list|,
literal|0.01d
argument_list|,
literal|0.05d
argument_list|,
literal|0.25d
argument_list|,
literal|0.50d
argument_list|,
literal|0.75d
argument_list|,
literal|0.95d
argument_list|,
literal|0.99d
argument_list|)
decl_stmt|;
name|double
index|[]
name|percents
init|=
operator|new
name|double
index|[
name|randomCdfValues
operator|.
name|size
argument_list|()
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
name|randomCdfValues
operator|.
name|size
argument_list|()
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
name|randomCdfValues
operator|.
name|get
argument_list|(
name|i
argument_list|)
expr_stmt|;
block|}
return|return
name|percents
return|;
block|}
block|}
end_class

end_unit
