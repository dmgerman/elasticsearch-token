begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.range
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
operator|.
name|range
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
name|InternalAggregation
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
name|org
operator|.
name|junit
operator|.
name|Before
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeMap
import|;
end_import

begin_class
DECL|class|InternalRangeTestCase
specifier|public
specifier|abstract
class|class
name|InternalRangeTestCase
parameter_list|<
name|T
extends|extends
name|InternalAggregation
operator|&
name|Range
parameter_list|>
extends|extends
name|InternalAggregationTestCase
argument_list|<
name|T
argument_list|>
block|{
DECL|field|keyed
specifier|private
name|boolean
name|keyed
decl_stmt|;
annotation|@
name|Override
annotation|@
name|Before
DECL|method|setUp
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|setUp
argument_list|()
expr_stmt|;
name|keyed
operator|=
name|randomBoolean
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createTestInstance
specifier|protected
name|T
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
return|return
name|createTestInstance
argument_list|(
name|name
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|,
name|keyed
argument_list|)
return|;
block|}
DECL|method|createTestInstance
specifier|protected
specifier|abstract
name|T
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
parameter_list|)
function_decl|;
annotation|@
name|Override
DECL|method|assertReduced
specifier|protected
name|void
name|assertReduced
parameter_list|(
name|T
name|reduced
parameter_list|,
name|List
argument_list|<
name|T
argument_list|>
name|inputs
parameter_list|)
block|{
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|expectedCounts
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|T
name|input
range|:
name|inputs
control|)
block|{
for|for
control|(
name|Range
operator|.
name|Bucket
name|bucket
range|:
name|input
operator|.
name|getBuckets
argument_list|()
control|)
block|{
name|expectedCounts
operator|.
name|compute
argument_list|(
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|,
parameter_list|(
name|key
parameter_list|,
name|oldValue
parameter_list|)
lambda|->
operator|(
name|oldValue
operator|==
literal|null
condition|?
literal|0
else|:
name|oldValue
operator|)
operator|+
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|actualCounts
init|=
operator|new
name|TreeMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Range
operator|.
name|Bucket
name|bucket
range|:
name|reduced
operator|.
name|getBuckets
argument_list|()
control|)
block|{
name|actualCounts
operator|.
name|compute
argument_list|(
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|,
parameter_list|(
name|key
parameter_list|,
name|oldValue
parameter_list|)
lambda|->
operator|(
name|oldValue
operator|==
literal|null
condition|?
literal|0
else|:
name|oldValue
operator|)
operator|+
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|expectedCounts
argument_list|,
name|actualCounts
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
