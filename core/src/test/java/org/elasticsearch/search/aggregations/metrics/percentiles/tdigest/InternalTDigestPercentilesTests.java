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
name|metrics
operator|.
name|percentiles
operator|.
name|InternalPercentilesTestCase
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
name|ParsedPercentiles
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
name|Arrays
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
name|InternalPercentilesTestCase
argument_list|<
name|InternalTDigestPercentiles
argument_list|>
block|{
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
parameter_list|,
name|boolean
name|keyed
parameter_list|,
name|DocValueFormat
name|format
parameter_list|,
name|double
index|[]
name|percents
parameter_list|,
name|double
index|[]
name|values
parameter_list|)
block|{
specifier|final
name|TDigestState
name|state
init|=
operator|new
name|TDigestState
argument_list|(
literal|100
argument_list|)
decl_stmt|;
name|Arrays
operator|.
name|stream
argument_list|(
name|values
argument_list|)
operator|.
name|forEach
argument_list|(
name|state
operator|::
name|add
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|state
operator|.
name|centroidCount
argument_list|()
argument_list|,
name|values
operator|.
name|length
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
annotation|@
name|Override
DECL|method|implementationClass
specifier|protected
name|Class
argument_list|<
name|?
extends|extends
name|ParsedPercentiles
argument_list|>
name|implementationClass
parameter_list|()
block|{
return|return
name|ParsedTDigestPercentiles
operator|.
name|class
return|;
block|}
block|}
end_class

end_unit

