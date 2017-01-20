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
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|Document
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|LongPoint
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|SortedNumericDocValuesField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|DirectoryReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|IndexReader
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|RandomIndexWriter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|IndexSearcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|MatchAllDocsQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|MatchNoDocsQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Query
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|store
operator|.
name|Directory
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
name|AggregatorTestCase
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
name|Arrays
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
name|function
operator|.
name|Consumer
import|;
end_import

begin_class
DECL|class|DateHistogramAggregatorTests
specifier|public
class|class
name|DateHistogramAggregatorTests
extends|extends
name|AggregatorTestCase
block|{
DECL|field|DATE_FIELD
specifier|private
specifier|static
specifier|final
name|String
name|DATE_FIELD
init|=
literal|"date"
decl_stmt|;
DECL|field|INSTANT_FIELD
specifier|private
specifier|static
specifier|final
name|String
name|INSTANT_FIELD
init|=
literal|"instant"
decl_stmt|;
DECL|field|dataset
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|dataset
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"2010-03-12T01:07:45"
argument_list|,
literal|"2010-04-27T03:43:34"
argument_list|,
literal|"2012-05-18T04:11:00"
argument_list|,
literal|"2013-05-29T05:11:31"
argument_list|,
literal|"2013-10-31T08:24:05"
argument_list|,
literal|"2015-02-13T13:09:32"
argument_list|,
literal|"2015-06-24T13:47:43"
argument_list|,
literal|"2015-11-13T16:14:34"
argument_list|,
literal|"2016-03-04T17:09:50"
argument_list|,
literal|"2017-12-12T22:55:46"
argument_list|)
decl_stmt|;
DECL|method|testMatchNoDocs
specifier|public
name|void
name|testMatchNoDocs
parameter_list|()
throws|throws
name|IOException
block|{
name|testBothCases
argument_list|(
operator|new
name|MatchNoDocsQuery
argument_list|()
argument_list|,
name|dataset
argument_list|,
name|aggregation
lambda|->
name|aggregation
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|YEAR
argument_list|)
operator|.
name|field
argument_list|(
name|DATE_FIELD
argument_list|)
argument_list|,
name|histogram
lambda|->
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|histogram
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testMatchAllDocs
specifier|public
name|void
name|testMatchAllDocs
parameter_list|()
throws|throws
name|IOException
block|{
name|Query
name|query
init|=
operator|new
name|MatchAllDocsQuery
argument_list|()
decl_stmt|;
name|testSearchCase
argument_list|(
name|query
argument_list|,
name|dataset
argument_list|,
name|aggregation
lambda|->
name|aggregation
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|YEAR
argument_list|)
operator|.
name|field
argument_list|(
name|DATE_FIELD
argument_list|)
argument_list|,
name|histogram
lambda|->
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|histogram
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|testSearchAndReduceCase
argument_list|(
name|query
argument_list|,
name|dataset
argument_list|,
name|aggregation
lambda|->
name|aggregation
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|YEAR
argument_list|)
operator|.
name|field
argument_list|(
name|DATE_FIELD
argument_list|)
argument_list|,
name|histogram
lambda|->
name|assertEquals
argument_list|(
literal|8
argument_list|,
name|histogram
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|testBothCases
argument_list|(
name|query
argument_list|,
name|dataset
argument_list|,
name|aggregation
lambda|->
name|aggregation
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|YEAR
argument_list|)
operator|.
name|field
argument_list|(
name|DATE_FIELD
argument_list|)
operator|.
name|minDocCount
argument_list|(
literal|1L
argument_list|)
argument_list|,
name|histogram
lambda|->
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|histogram
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoDocs
specifier|public
name|void
name|testNoDocs
parameter_list|()
throws|throws
name|IOException
block|{
name|Query
name|query
init|=
operator|new
name|MatchNoDocsQuery
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|dates
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
name|Consumer
argument_list|<
name|DateHistogramAggregationBuilder
argument_list|>
name|aggregation
init|=
name|agg
lambda|->
name|agg
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|YEAR
argument_list|)
operator|.
name|field
argument_list|(
name|DATE_FIELD
argument_list|)
decl_stmt|;
name|testSearchCase
argument_list|(
name|query
argument_list|,
name|dates
argument_list|,
name|aggregation
argument_list|,
name|histogram
lambda|->
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|histogram
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|testSearchAndReduceCase
argument_list|(
name|query
argument_list|,
name|dates
argument_list|,
name|aggregation
argument_list|,
name|histogram
lambda|->
name|assertNull
argument_list|(
name|histogram
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testAggregateWrongField
specifier|public
name|void
name|testAggregateWrongField
parameter_list|()
throws|throws
name|IOException
block|{
name|testBothCases
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|dataset
argument_list|,
name|aggregation
lambda|->
name|aggregation
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|YEAR
argument_list|)
operator|.
name|field
argument_list|(
literal|"wrong_field"
argument_list|)
argument_list|,
name|histogram
lambda|->
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|histogram
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIntervalYear
specifier|public
name|void
name|testIntervalYear
parameter_list|()
throws|throws
name|IOException
block|{
name|testBothCases
argument_list|(
name|LongPoint
operator|.
name|newRangeQuery
argument_list|(
name|INSTANT_FIELD
argument_list|,
name|asLong
argument_list|(
literal|"2015-01-01"
argument_list|)
argument_list|,
name|asLong
argument_list|(
literal|"2017-12-31"
argument_list|)
argument_list|)
argument_list|,
name|dataset
argument_list|,
name|aggregation
lambda|->
name|aggregation
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|YEAR
argument_list|)
operator|.
name|field
argument_list|(
name|DATE_FIELD
argument_list|)
argument_list|,
name|histogram
lambda|->
block|{
name|List
argument_list|<
name|Histogram
operator|.
name|Bucket
argument_list|>
name|buckets
operator|=
name|histogram
operator|.
name|getBuckets
argument_list|()
argument_list|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
argument_list|;
name|Histogram
operator|.
name|Bucket
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2015-01-01T00:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2016-01-01T00:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-01-01T00:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
block|}
block|)
class|;
end_class

begin_function
unit|}      public
DECL|method|testIntervalMonth
name|void
name|testIntervalMonth
parameter_list|()
throws|throws
name|IOException
block|{
name|testBothCases
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"2017-01-01"
argument_list|,
literal|"2017-02-02"
argument_list|,
literal|"2017-02-03"
argument_list|,
literal|"2017-03-04"
argument_list|,
literal|"2017-03-05"
argument_list|,
literal|"2017-03-06"
argument_list|)
argument_list|,
name|aggregation
lambda|->
name|aggregation
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|MONTH
argument_list|)
operator|.
name|field
argument_list|(
name|DATE_FIELD
argument_list|)
argument_list|,
name|histogram
lambda|->
block|{
name|List
argument_list|<
name|Histogram
operator|.
name|Bucket
argument_list|>
name|buckets
operator|=
name|histogram
operator|.
name|getBuckets
argument_list|()
argument_list|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
argument_list|;
name|Histogram
operator|.
name|Bucket
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-01-01T00:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T00:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-03-01T00:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
block|}
end_function

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_function
unit|}      public
DECL|method|testIntervalDay
name|void
name|testIntervalDay
parameter_list|()
throws|throws
name|IOException
block|{
name|testBothCases
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"2017-02-01"
argument_list|,
literal|"2017-02-02"
argument_list|,
literal|"2017-02-02"
argument_list|,
literal|"2017-02-03"
argument_list|,
literal|"2017-02-03"
argument_list|,
literal|"2017-02-03"
argument_list|,
literal|"2017-02-05"
argument_list|)
argument_list|,
name|aggregation
lambda|->
name|aggregation
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|DAY
argument_list|)
operator|.
name|field
argument_list|(
name|DATE_FIELD
argument_list|)
operator|.
name|minDocCount
argument_list|(
literal|1L
argument_list|)
argument_list|,
name|histogram
lambda|->
block|{
name|List
argument_list|<
name|Histogram
operator|.
name|Bucket
argument_list|>
name|buckets
operator|=
name|histogram
operator|.
name|getBuckets
argument_list|()
argument_list|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
argument_list|;
name|Histogram
operator|.
name|Bucket
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T00:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-02T00:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-03T00:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-05T00:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
block|}
end_function

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_function
unit|}      public
DECL|method|testIntervalHour
name|void
name|testIntervalHour
parameter_list|()
throws|throws
name|IOException
block|{
name|testBothCases
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"2017-02-01T09:02:00.000Z"
argument_list|,
literal|"2017-02-01T09:35:00.000Z"
argument_list|,
literal|"2017-02-01T10:15:00.000Z"
argument_list|,
literal|"2017-02-01T13:06:00.000Z"
argument_list|,
literal|"2017-02-01T14:04:00.000Z"
argument_list|,
literal|"2017-02-01T14:05:00.000Z"
argument_list|,
literal|"2017-02-01T15:59:00.000Z"
argument_list|,
literal|"2017-02-01T16:06:00.000Z"
argument_list|,
literal|"2017-02-01T16:48:00.000Z"
argument_list|,
literal|"2017-02-01T16:59:00.000Z"
argument_list|)
argument_list|,
name|aggregation
lambda|->
name|aggregation
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|HOUR
argument_list|)
operator|.
name|field
argument_list|(
name|DATE_FIELD
argument_list|)
operator|.
name|minDocCount
argument_list|(
literal|1L
argument_list|)
argument_list|,
name|histogram
lambda|->
block|{
name|List
argument_list|<
name|Histogram
operator|.
name|Bucket
argument_list|>
name|buckets
operator|=
name|histogram
operator|.
name|getBuckets
argument_list|()
argument_list|;
name|assertEquals
argument_list|(
literal|6
argument_list|,
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
argument_list|;
name|Histogram
operator|.
name|Bucket
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T09:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T10:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T13:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T14:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|4
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T15:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|5
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T16:00:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
block|}
end_function

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_function
unit|}      public
DECL|method|testIntervalMinute
name|void
name|testIntervalMinute
parameter_list|()
throws|throws
name|IOException
block|{
name|testBothCases
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"2017-02-01T09:02:35.000Z"
argument_list|,
literal|"2017-02-01T09:02:59.000Z"
argument_list|,
literal|"2017-02-01T09:15:37.000Z"
argument_list|,
literal|"2017-02-01T09:16:04.000Z"
argument_list|,
literal|"2017-02-01T09:16:42.000Z"
argument_list|)
argument_list|,
name|aggregation
lambda|->
name|aggregation
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|MINUTE
argument_list|)
operator|.
name|field
argument_list|(
name|DATE_FIELD
argument_list|)
operator|.
name|minDocCount
argument_list|(
literal|1L
argument_list|)
argument_list|,
name|histogram
lambda|->
block|{
name|List
argument_list|<
name|Histogram
operator|.
name|Bucket
argument_list|>
name|buckets
operator|=
name|histogram
operator|.
name|getBuckets
argument_list|()
argument_list|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
argument_list|;
name|Histogram
operator|.
name|Bucket
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T09:02:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T09:15:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T09:16:00.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
block|}
end_function

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_function
unit|}      public
DECL|method|testIntervalSecond
name|void
name|testIntervalSecond
parameter_list|()
throws|throws
name|IOException
block|{
name|testBothCases
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
literal|"2017-02-01T00:00:05.015Z"
argument_list|,
literal|"2017-02-01T00:00:11.299Z"
argument_list|,
literal|"2017-02-01T00:00:11.074Z"
argument_list|,
literal|"2017-02-01T00:00:37.688Z"
argument_list|,
literal|"2017-02-01T00:00:37.210Z"
argument_list|,
literal|"2017-02-01T00:00:37.380Z"
argument_list|)
argument_list|,
name|aggregation
lambda|->
name|aggregation
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|SECOND
argument_list|)
operator|.
name|field
argument_list|(
name|DATE_FIELD
argument_list|)
operator|.
name|minDocCount
argument_list|(
literal|1L
argument_list|)
argument_list|,
name|histogram
lambda|->
block|{
name|List
argument_list|<
name|Histogram
operator|.
name|Bucket
argument_list|>
name|buckets
operator|=
name|histogram
operator|.
name|getBuckets
argument_list|()
argument_list|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
argument_list|;
name|Histogram
operator|.
name|Bucket
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T00:00:05.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T00:00:11.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T00:00:37.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
block|}
end_function

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_function
unit|}      public
DECL|method|testMinDocCount
name|void
name|testMinDocCount
parameter_list|()
throws|throws
name|IOException
block|{
name|Query
name|query
init|=
name|LongPoint
operator|.
name|newRangeQuery
argument_list|(
name|INSTANT_FIELD
argument_list|,
name|asLong
argument_list|(
literal|"2017-02-01T00:00:00.000Z"
argument_list|)
argument_list|,
name|asLong
argument_list|(
literal|"2017-02-01T00:00:30.000Z"
argument_list|)
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|timestamps
init|=
name|Arrays
operator|.
name|asList
argument_list|(
literal|"2017-02-01T00:00:05.015Z"
argument_list|,
literal|"2017-02-01T00:00:11.299Z"
argument_list|,
literal|"2017-02-01T00:00:11.074Z"
argument_list|,
literal|"2017-02-01T00:00:13.688Z"
argument_list|,
literal|"2017-02-01T00:00:21.380Z"
argument_list|)
decl_stmt|;
comment|// 5 sec interval with minDocCount = 0
name|testSearchAndReduceCase
argument_list|(
name|query
argument_list|,
name|timestamps
argument_list|,
name|aggregation
lambda|->
name|aggregation
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|seconds
argument_list|(
literal|5
argument_list|)
argument_list|)
operator|.
name|field
argument_list|(
name|DATE_FIELD
argument_list|)
operator|.
name|minDocCount
argument_list|(
literal|0L
argument_list|)
argument_list|,
name|histogram
lambda|->
block|{
name|List
argument_list|<
name|Histogram
operator|.
name|Bucket
argument_list|>
name|buckets
operator|=
name|histogram
operator|.
name|getBuckets
argument_list|()
argument_list|;
name|assertEquals
argument_list|(
literal|4
argument_list|,
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
argument_list|;
name|Histogram
operator|.
name|Bucket
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T00:00:05.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T00:00:10.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T00:00:15.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T00:00:20.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
block|}
end_function

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_comment
comment|// 5 sec interval with minDocCount = 3
end_comment

begin_expr_stmt
name|testSearchAndReduceCase
argument_list|(
name|query
argument_list|,
name|timestamps
argument_list|,
name|aggregation
lambda|->
name|aggregation
operator|.
name|dateHistogramInterval
argument_list|(
name|DateHistogramInterval
operator|.
name|seconds
argument_list|(
literal|5
argument_list|)
argument_list|)
operator|.
name|field
argument_list|(
name|DATE_FIELD
argument_list|)
operator|.
name|minDocCount
argument_list|(
literal|3L
argument_list|)
argument_list|,
name|histogram
lambda|->
block|{
name|List
argument_list|<
name|Histogram
operator|.
name|Bucket
argument_list|>
name|buckets
operator|=
name|histogram
operator|.
name|getBuckets
argument_list|()
argument_list|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|buckets
operator|.
name|size
argument_list|()
argument_list|)
argument_list|;
name|Histogram
operator|.
name|Bucket
name|bucket
operator|=
name|buckets
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|"2017-02-01T00:00:10.000Z"
argument_list|,
name|bucket
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
argument_list|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|;
end_expr_stmt

begin_empty_stmt
unit|}         )
empty_stmt|;
end_empty_stmt

begin_function
unit|}      private
DECL|method|testSearchCase
name|void
name|testSearchCase
parameter_list|(
name|Query
name|query
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|dataset
parameter_list|,
name|Consumer
argument_list|<
name|DateHistogramAggregationBuilder
argument_list|>
name|configure
parameter_list|,
name|Consumer
argument_list|<
name|Histogram
argument_list|>
name|verify
parameter_list|)
throws|throws
name|IOException
block|{
name|executeTestCase
argument_list|(
literal|false
argument_list|,
name|query
argument_list|,
name|dataset
argument_list|,
name|configure
argument_list|,
name|verify
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
DECL|method|testSearchAndReduceCase
specifier|private
name|void
name|testSearchAndReduceCase
parameter_list|(
name|Query
name|query
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|dataset
parameter_list|,
name|Consumer
argument_list|<
name|DateHistogramAggregationBuilder
argument_list|>
name|configure
parameter_list|,
name|Consumer
argument_list|<
name|Histogram
argument_list|>
name|verify
parameter_list|)
throws|throws
name|IOException
block|{
name|executeTestCase
argument_list|(
literal|true
argument_list|,
name|query
argument_list|,
name|dataset
argument_list|,
name|configure
argument_list|,
name|verify
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
DECL|method|testBothCases
specifier|private
name|void
name|testBothCases
parameter_list|(
name|Query
name|query
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|dataset
parameter_list|,
name|Consumer
argument_list|<
name|DateHistogramAggregationBuilder
argument_list|>
name|configure
parameter_list|,
name|Consumer
argument_list|<
name|Histogram
argument_list|>
name|verify
parameter_list|)
throws|throws
name|IOException
block|{
name|testSearchCase
argument_list|(
name|query
argument_list|,
name|dataset
argument_list|,
name|configure
argument_list|,
name|verify
argument_list|)
expr_stmt|;
name|testSearchAndReduceCase
argument_list|(
name|query
argument_list|,
name|dataset
argument_list|,
name|configure
argument_list|,
name|verify
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
DECL|method|executeTestCase
specifier|private
name|void
name|executeTestCase
parameter_list|(
name|boolean
name|reduced
parameter_list|,
name|Query
name|query
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|dataset
parameter_list|,
name|Consumer
argument_list|<
name|DateHistogramAggregationBuilder
argument_list|>
name|configure
parameter_list|,
name|Consumer
argument_list|<
name|Histogram
argument_list|>
name|verify
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Directory
name|directory
init|=
name|newDirectory
argument_list|()
init|)
block|{
try|try
init|(
name|RandomIndexWriter
name|indexWriter
init|=
operator|new
name|RandomIndexWriter
argument_list|(
name|random
argument_list|()
argument_list|,
name|directory
argument_list|)
init|)
block|{
name|Document
name|document
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|date
range|:
name|dataset
control|)
block|{
if|if
condition|(
name|frequently
argument_list|()
condition|)
block|{
name|indexWriter
operator|.
name|commit
argument_list|()
expr_stmt|;
block|}
name|long
name|instant
init|=
name|asLong
argument_list|(
name|date
argument_list|)
decl_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|SortedNumericDocValuesField
argument_list|(
name|DATE_FIELD
argument_list|,
name|instant
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|LongPoint
argument_list|(
name|INSTANT_FIELD
argument_list|,
name|instant
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|document
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
try|try
init|(
name|IndexReader
name|indexReader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|directory
argument_list|)
init|)
block|{
name|IndexSearcher
name|indexSearcher
init|=
name|newSearcher
argument_list|(
name|indexReader
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|DateHistogramAggregationBuilder
name|aggregationBuilder
init|=
operator|new
name|DateHistogramAggregationBuilder
argument_list|(
literal|"_name"
argument_list|)
decl_stmt|;
if|if
condition|(
name|configure
operator|!=
literal|null
condition|)
block|{
name|configure
operator|.
name|accept
argument_list|(
name|aggregationBuilder
argument_list|)
expr_stmt|;
block|}
name|DateFieldMapper
operator|.
name|Builder
name|builder
init|=
operator|new
name|DateFieldMapper
operator|.
name|Builder
argument_list|(
literal|"_name"
argument_list|)
decl_stmt|;
name|DateFieldMapper
operator|.
name|DateFieldType
name|fieldType
init|=
name|builder
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|fieldType
operator|.
name|setHasDocValues
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|fieldType
operator|.
name|setName
argument_list|(
name|aggregationBuilder
operator|.
name|field
argument_list|()
argument_list|)
expr_stmt|;
name|InternalDateHistogram
name|histogram
decl_stmt|;
if|if
condition|(
name|reduced
condition|)
block|{
name|histogram
operator|=
name|searchAndReduce
argument_list|(
name|indexSearcher
argument_list|,
name|query
argument_list|,
name|aggregationBuilder
argument_list|,
name|fieldType
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|histogram
operator|=
name|search
argument_list|(
name|indexSearcher
argument_list|,
name|query
argument_list|,
name|aggregationBuilder
argument_list|,
name|fieldType
argument_list|)
expr_stmt|;
block|}
name|verify
operator|.
name|accept
argument_list|(
name|histogram
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_function

begin_function
DECL|method|asLong
specifier|private
specifier|static
name|long
name|asLong
parameter_list|(
name|String
name|dateTime
parameter_list|)
block|{
return|return
name|DateFieldMapper
operator|.
name|DEFAULT_DATE_TIME_FORMATTER
operator|.
name|parser
argument_list|()
operator|.
name|parseDateTime
argument_list|(
name|dateTime
argument_list|)
operator|.
name|getMillis
argument_list|()
return|;
block|}
end_function

unit|}
end_unit

