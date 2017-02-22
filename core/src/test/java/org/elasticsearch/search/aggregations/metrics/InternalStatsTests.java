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
name|metrics
operator|.
name|stats
operator|.
name|InternalStats
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
DECL|class|InternalStatsTests
specifier|public
class|class
name|InternalStatsTests
extends|extends
name|InternalAggregationTestCase
argument_list|<
name|InternalStats
argument_list|>
block|{
annotation|@
name|Override
DECL|method|createTestInstance
specifier|protected
name|InternalStats
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
name|long
name|count
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|50
argument_list|)
decl_stmt|;
name|double
index|[]
name|minMax
init|=
operator|new
name|double
index|[
literal|2
index|]
decl_stmt|;
name|minMax
index|[
literal|0
index|]
operator|=
name|randomDouble
argument_list|()
expr_stmt|;
name|minMax
index|[
literal|0
index|]
operator|=
name|randomDouble
argument_list|()
expr_stmt|;
name|double
name|sum
init|=
name|randomDoubleBetween
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|,
literal|true
argument_list|)
decl_stmt|;
return|return
operator|new
name|InternalStats
argument_list|(
name|name
argument_list|,
name|count
argument_list|,
name|sum
argument_list|,
name|minMax
index|[
literal|0
index|]
argument_list|,
name|minMax
index|[
literal|1
index|]
argument_list|,
name|DocValueFormat
operator|.
name|RAW
argument_list|,
name|pipelineAggregators
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
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
name|InternalStats
name|reduced
parameter_list|,
name|List
argument_list|<
name|InternalStats
argument_list|>
name|inputs
parameter_list|)
block|{
name|long
name|expectedCount
init|=
literal|0
decl_stmt|;
name|double
name|expectedSum
init|=
literal|0
decl_stmt|;
name|double
name|expectedMin
init|=
name|Double
operator|.
name|POSITIVE_INFINITY
decl_stmt|;
name|double
name|expectedMax
init|=
name|Double
operator|.
name|NEGATIVE_INFINITY
decl_stmt|;
for|for
control|(
name|InternalStats
name|stats
range|:
name|inputs
control|)
block|{
name|expectedCount
operator|+=
name|stats
operator|.
name|getCount
argument_list|()
expr_stmt|;
if|if
condition|(
name|Double
operator|.
name|compare
argument_list|(
name|stats
operator|.
name|getMin
argument_list|()
argument_list|,
name|expectedMin
argument_list|)
operator|<
literal|0
condition|)
block|{
name|expectedMin
operator|=
name|stats
operator|.
name|getMin
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|Double
operator|.
name|compare
argument_list|(
name|stats
operator|.
name|getMax
argument_list|()
argument_list|,
name|expectedMax
argument_list|)
operator|>
literal|0
condition|)
block|{
name|expectedMax
operator|=
name|stats
operator|.
name|getMax
argument_list|()
expr_stmt|;
block|}
name|expectedSum
operator|+=
name|stats
operator|.
name|getSum
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expectedCount
argument_list|,
name|reduced
operator|.
name|getCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedSum
argument_list|,
name|reduced
operator|.
name|getSum
argument_list|()
argument_list|,
literal|1e-10
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedMin
argument_list|,
name|reduced
operator|.
name|getMin
argument_list|()
argument_list|,
literal|0d
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedMax
argument_list|,
name|reduced
operator|.
name|getMax
argument_list|()
argument_list|,
literal|0d
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|instanceReader
specifier|protected
name|Writeable
operator|.
name|Reader
argument_list|<
name|InternalStats
argument_list|>
name|instanceReader
parameter_list|()
block|{
return|return
name|InternalStats
operator|::
operator|new
return|;
block|}
block|}
end_class

end_unit
