begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.histogram
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
name|histogram
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
name|InternalMultiBucketAggregationTestCase
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
name|ParsedMultiBucketAggregation
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
name|search
operator|.
name|aggregations
operator|.
name|BucketOrder
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
name|joda
operator|.
name|time
operator|.
name|DateTime
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueHours
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueMinutes
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
operator|.
name|timeValueSeconds
import|;
end_import

begin_class
DECL|class|InternalDateHistogramTests
specifier|public
class|class
name|InternalDateHistogramTests
extends|extends
name|InternalMultiBucketAggregationTestCase
argument_list|<
name|InternalDateHistogram
argument_list|>
block|{
DECL|field|keyed
specifier|private
name|boolean
name|keyed
decl_stmt|;
DECL|field|format
specifier|private
name|DocValueFormat
name|format
decl_stmt|;
annotation|@
name|Override
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
name|format
operator|=
name|randomNumericDocValueFormat
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createTestInstance
specifier|protected
name|InternalDateHistogram
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
name|InternalAggregations
name|aggregations
parameter_list|)
block|{
name|int
name|nbBuckets
init|=
name|randomInt
argument_list|(
literal|10
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|InternalDateHistogram
operator|.
name|Bucket
argument_list|>
name|buckets
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|nbBuckets
argument_list|)
decl_stmt|;
name|long
name|startingDate
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|interval
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|long
name|intervalMillis
init|=
name|randomFrom
argument_list|(
name|timeValueSeconds
argument_list|(
name|interval
argument_list|)
argument_list|,
name|timeValueMinutes
argument_list|(
name|interval
argument_list|)
argument_list|,
name|timeValueHours
argument_list|(
name|interval
argument_list|)
argument_list|)
operator|.
name|getMillis
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
name|nbBuckets
condition|;
name|i
operator|++
control|)
block|{
name|long
name|key
init|=
name|startingDate
operator|+
operator|(
name|intervalMillis
operator|*
name|i
operator|)
decl_stmt|;
name|buckets
operator|.
name|add
argument_list|(
name|i
argument_list|,
operator|new
name|InternalDateHistogram
operator|.
name|Bucket
argument_list|(
name|key
argument_list|,
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
argument_list|,
name|keyed
argument_list|,
name|format
argument_list|,
name|aggregations
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|BucketOrder
name|order
init|=
name|randomFrom
argument_list|(
name|BucketOrder
operator|.
name|key
argument_list|(
literal|true
argument_list|)
argument_list|,
name|BucketOrder
operator|.
name|key
argument_list|(
literal|false
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|InternalDateHistogram
argument_list|(
name|name
argument_list|,
name|buckets
argument_list|,
name|order
argument_list|,
literal|1
argument_list|,
literal|0L
argument_list|,
literal|null
argument_list|,
name|format
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
name|InternalDateHistogram
name|reduced
parameter_list|,
name|List
argument_list|<
name|InternalDateHistogram
argument_list|>
name|inputs
parameter_list|)
block|{
name|Map
argument_list|<
name|Long
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
name|Histogram
name|histogram
range|:
name|inputs
control|)
block|{
for|for
control|(
name|Histogram
operator|.
name|Bucket
name|bucket
range|:
name|histogram
operator|.
name|getBuckets
argument_list|()
control|)
block|{
name|expectedCounts
operator|.
name|compute
argument_list|(
operator|(
operator|(
name|DateTime
operator|)
name|bucket
operator|.
name|getKey
argument_list|()
operator|)
operator|.
name|getMillis
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
name|Map
argument_list|<
name|Long
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
name|Histogram
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
operator|(
operator|(
name|DateTime
operator|)
name|bucket
operator|.
name|getKey
argument_list|()
operator|)
operator|.
name|getMillis
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
name|Writeable
operator|.
name|Reader
argument_list|<
name|InternalDateHistogram
argument_list|>
name|instanceReader
parameter_list|()
block|{
return|return
name|InternalDateHistogram
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
name|ParsedMultiBucketAggregation
argument_list|>
name|implementationClass
parameter_list|()
block|{
return|return
name|ParsedDateHistogram
operator|.
name|class
return|;
block|}
block|}
end_class

end_unit

