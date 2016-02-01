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
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|index
operator|.
name|IndexRequestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
operator|.
name|SearchResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|core
operator|.
name|DateFieldMapper
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
name|DateHistogramInterval
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
name|ESIntegTestCase
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
name|transport
operator|.
name|AssertingLocalTransport
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
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|io
operator|.
name|IOException
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
name|concurrent
operator|.
name|ExecutionException
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
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|matchAllQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregationBuilders
operator|.
name|dateHistogram
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|core
operator|.
name|IsNull
operator|.
name|notNullValue
import|;
end_import

begin_comment
comment|/**  * The serialisation of offsets for the date histogram aggregation was corrected in version 1.4 to allow negative offsets and as such the  * serialisation of negative offsets in these tests would break in pre 1.4 versions.  These tests are separated from the other DateHistogramTests so the  * AssertingLocalTransport for these tests can be set to only use versions 1.4 onwards while keeping the other tests using all versions  */
end_comment

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|SuiteScopeTestCase
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|SUITE
argument_list|)
DECL|class|DateHistogramOffsetIT
specifier|public
class|class
name|DateHistogramOffsetIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|DATE_FORMAT
specifier|private
specifier|static
specifier|final
name|String
name|DATE_FORMAT
init|=
literal|"yyyy-MM-dd:hh-mm-ss"
decl_stmt|;
DECL|method|date
specifier|private
name|DateTime
name|date
parameter_list|(
name|String
name|date
parameter_list|)
block|{
return|return
name|DateFieldMapper
operator|.
name|Defaults
operator|.
name|DATE_TIME_FORMATTER
operator|.
name|parser
argument_list|()
operator|.
name|parseDateTime
argument_list|(
name|date
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
name|AssertingLocalTransport
operator|.
name|ASSERTING_TRANSPORT_MIN_VERSION_KEY
operator|.
name|getKey
argument_list|()
argument_list|,
name|Version
operator|.
name|V_1_4_0_Beta1
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Before
DECL|method|beforeEachTest
specifier|public
name|void
name|beforeEachTest
parameter_list|()
throws|throws
name|IOException
block|{
name|prepareCreate
argument_list|(
literal|"idx2"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
literal|"date"
argument_list|,
literal|"type=date"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
annotation|@
name|After
DECL|method|afterEachTest
specifier|public
name|void
name|afterEachTest
parameter_list|()
throws|throws
name|IOException
block|{
name|internalCluster
argument_list|()
operator|.
name|wipeIndices
argument_list|(
literal|"idx2"
argument_list|)
expr_stmt|;
block|}
DECL|method|prepareIndex
specifier|private
name|void
name|prepareIndex
parameter_list|(
name|DateTime
name|date
parameter_list|,
name|int
name|numHours
parameter_list|,
name|int
name|stepSizeHours
parameter_list|,
name|int
name|idxIdStart
parameter_list|)
throws|throws
name|IOException
throws|,
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|IndexRequestBuilder
index|[]
name|reqs
init|=
operator|new
name|IndexRequestBuilder
index|[
name|numHours
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|idxIdStart
init|;
name|i
operator|<
name|idxIdStart
operator|+
name|reqs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|reqs
index|[
name|i
operator|-
name|idxIdStart
index|]
operator|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"idx2"
argument_list|,
literal|"type"
argument_list|,
literal|""
operator|+
name|i
argument_list|)
operator|.
name|setSource
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"date"
argument_list|,
name|date
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
name|date
operator|=
name|date
operator|.
name|plusHours
argument_list|(
name|stepSizeHours
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|reqs
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleValueWithPositiveOffset
specifier|public
name|void
name|testSingleValueWithPositiveOffset
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareIndex
argument_list|(
name|date
argument_list|(
literal|"2014-03-11T00:00:00+00:00"
argument_list|)
argument_list|,
literal|5
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx2"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|dateHistogram
argument_list|(
literal|"date_histo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"date"
argument_list|)
operator|.
name|offset
argument_list|(
literal|"2h"
argument_list|)
operator|.
name|format
argument_list|(
name|DATE_FORMAT
argument_list|)
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|DAY
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5L
argument_list|)
argument_list|)
expr_stmt|;
name|Histogram
name|histo
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"date_histo"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Histogram
operator|.
name|Bucket
argument_list|>
name|buckets
init|=
name|histo
operator|.
name|getBuckets
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|buckets
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|checkBucketFor
argument_list|(
name|buckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|DateTime
argument_list|(
literal|2014
argument_list|,
literal|3
argument_list|,
literal|10
argument_list|,
literal|2
argument_list|,
literal|0
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
argument_list|,
literal|2L
argument_list|)
expr_stmt|;
name|checkBucketFor
argument_list|(
name|buckets
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
operator|new
name|DateTime
argument_list|(
literal|2014
argument_list|,
literal|3
argument_list|,
literal|11
argument_list|,
literal|2
argument_list|,
literal|0
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
argument_list|,
literal|3L
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleValueWithNegativeOffset
specifier|public
name|void
name|testSingleValueWithNegativeOffset
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareIndex
argument_list|(
name|date
argument_list|(
literal|"2014-03-11T00:00:00+00:00"
argument_list|)
argument_list|,
literal|5
argument_list|,
operator|-
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx2"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|dateHistogram
argument_list|(
literal|"date_histo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"date"
argument_list|)
operator|.
name|offset
argument_list|(
literal|"-2h"
argument_list|)
operator|.
name|format
argument_list|(
name|DATE_FORMAT
argument_list|)
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|DAY
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5L
argument_list|)
argument_list|)
expr_stmt|;
name|Histogram
name|histo
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"date_histo"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Histogram
operator|.
name|Bucket
argument_list|>
name|buckets
init|=
name|histo
operator|.
name|getBuckets
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|buckets
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|checkBucketFor
argument_list|(
name|buckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|DateTime
argument_list|(
literal|2014
argument_list|,
literal|3
argument_list|,
literal|9
argument_list|,
literal|22
argument_list|,
literal|0
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
argument_list|,
literal|2L
argument_list|)
expr_stmt|;
name|checkBucketFor
argument_list|(
name|buckets
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
operator|new
name|DateTime
argument_list|(
literal|2014
argument_list|,
literal|3
argument_list|,
literal|10
argument_list|,
literal|22
argument_list|,
literal|0
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
argument_list|,
literal|3L
argument_list|)
expr_stmt|;
block|}
comment|/**      * Set offset so day buckets start at 6am. Index first 12 hours for two days, with one day gap.      */
DECL|method|testSingleValueWithOffsetMinDocCount
specifier|public
name|void
name|testSingleValueWithOffsetMinDocCount
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareIndex
argument_list|(
name|date
argument_list|(
literal|"2014-03-11T00:00:00+00:00"
argument_list|)
argument_list|,
literal|12
argument_list|,
literal|1
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|prepareIndex
argument_list|(
name|date
argument_list|(
literal|"2014-03-14T00:00:00+00:00"
argument_list|)
argument_list|,
literal|12
argument_list|,
literal|1
argument_list|,
literal|13
argument_list|)
expr_stmt|;
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx2"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|dateHistogram
argument_list|(
literal|"date_histo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"date"
argument_list|)
operator|.
name|offset
argument_list|(
literal|"6h"
argument_list|)
operator|.
name|minDocCount
argument_list|(
literal|0
argument_list|)
operator|.
name|format
argument_list|(
name|DATE_FORMAT
argument_list|)
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|DAY
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|24L
argument_list|)
argument_list|)
expr_stmt|;
name|Histogram
name|histo
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"date_histo"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|?
extends|extends
name|Histogram
operator|.
name|Bucket
argument_list|>
name|buckets
init|=
name|histo
operator|.
name|getBuckets
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|buckets
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|checkBucketFor
argument_list|(
name|buckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
operator|new
name|DateTime
argument_list|(
literal|2014
argument_list|,
literal|3
argument_list|,
literal|10
argument_list|,
literal|6
argument_list|,
literal|0
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
argument_list|,
literal|6L
argument_list|)
expr_stmt|;
name|checkBucketFor
argument_list|(
name|buckets
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|,
operator|new
name|DateTime
argument_list|(
literal|2014
argument_list|,
literal|3
argument_list|,
literal|11
argument_list|,
literal|6
argument_list|,
literal|0
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
argument_list|,
literal|6L
argument_list|)
expr_stmt|;
name|checkBucketFor
argument_list|(
name|buckets
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|,
operator|new
name|DateTime
argument_list|(
literal|2014
argument_list|,
literal|3
argument_list|,
literal|12
argument_list|,
literal|6
argument_list|,
literal|0
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
argument_list|,
literal|0L
argument_list|)
expr_stmt|;
name|checkBucketFor
argument_list|(
name|buckets
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|,
operator|new
name|DateTime
argument_list|(
literal|2014
argument_list|,
literal|3
argument_list|,
literal|13
argument_list|,
literal|6
argument_list|,
literal|0
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
argument_list|,
literal|6L
argument_list|)
expr_stmt|;
name|checkBucketFor
argument_list|(
name|buckets
operator|.
name|get
argument_list|(
literal|4
argument_list|)
argument_list|,
operator|new
name|DateTime
argument_list|(
literal|2014
argument_list|,
literal|3
argument_list|,
literal|14
argument_list|,
literal|6
argument_list|,
literal|0
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
argument_list|,
literal|6L
argument_list|)
expr_stmt|;
block|}
comment|/**      * @param bucket the bucket to check asssertions for      * @param key the expected key      * @param expectedSize the expected size of the bucket      */
DECL|method|checkBucketFor
specifier|private
specifier|static
name|void
name|checkBucketFor
parameter_list|(
name|Histogram
operator|.
name|Bucket
name|bucket
parameter_list|,
name|DateTime
name|key
parameter_list|,
name|long
name|expectedSize
parameter_list|)
block|{
name|assertThat
argument_list|(
name|bucket
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|key
operator|.
name|toString
argument_list|(
name|DATE_FORMAT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
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
argument_list|,
name|equalTo
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|expectedSize
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

