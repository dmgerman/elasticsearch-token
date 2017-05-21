begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.filters
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
name|filters
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
name|InternalAggregations
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
name|ArrayList
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
DECL|class|InternalFiltersTests
specifier|public
class|class
name|InternalFiltersTests
extends|extends
name|InternalAggregationTestCase
argument_list|<
name|InternalFilters
argument_list|>
block|{
DECL|field|keyed
specifier|private
name|boolean
name|keyed
decl_stmt|;
DECL|field|keys
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|keys
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|int
name|numKeys
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
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
name|numKeys
condition|;
name|i
operator|++
control|)
block|{
name|keys
operator|.
name|add
argument_list|(
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|createTestInstance
specifier|protected
name|InternalFilters
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
specifier|final
name|List
argument_list|<
name|InternalFilters
operator|.
name|InternalBucket
argument_list|>
name|buckets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|String
name|key
init|=
name|keys
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|int
name|docCount
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|1000
argument_list|)
decl_stmt|;
name|buckets
operator|.
name|add
argument_list|(
operator|new
name|InternalFilters
operator|.
name|InternalBucket
argument_list|(
name|key
argument_list|,
name|docCount
argument_list|,
name|InternalAggregations
operator|.
name|EMPTY
argument_list|,
name|keyed
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|InternalFilters
argument_list|(
name|name
argument_list|,
name|buckets
argument_list|,
name|keyed
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
name|InternalFilters
name|reduced
parameter_list|,
name|List
argument_list|<
name|InternalFilters
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
name|InternalFilters
name|input
range|:
name|inputs
control|)
block|{
for|for
control|(
name|InternalFilters
operator|.
name|InternalBucket
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
name|InternalFilters
operator|.
name|InternalBucket
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
annotation|@
name|Override
DECL|method|instanceReader
specifier|protected
name|Reader
argument_list|<
name|InternalFilters
argument_list|>
name|instanceReader
parameter_list|()
block|{
return|return
name|InternalFilters
operator|::
operator|new
return|;
block|}
block|}
end_class

end_unit
