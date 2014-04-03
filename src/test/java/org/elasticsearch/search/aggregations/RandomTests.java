begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|hppc
operator|.
name|IntOpenHashSet
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|util
operator|.
name|LuceneTestCase
operator|.
name|Slow
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
name|SearchRequestBuilder
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
name|action
operator|.
name|support
operator|.
name|IndicesOptions
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
name|xcontent
operator|.
name|XContentBuilder
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
name|query
operator|.
name|FilterBuilders
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
name|query
operator|.
name|RangeFilterBuilder
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
name|filter
operator|.
name|Filter
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
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|range
operator|.
name|Range
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
name|range
operator|.
name|RangeBuilder
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
name|terms
operator|.
name|Terms
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
name|terms
operator|.
name|TermsAggregatorFactory
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
name|ElasticsearchIntegrationTest
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
name|search
operator|.
name|aggregations
operator|.
name|AggregationBuilders
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAllSuccessful
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertNoFailures
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
comment|/**  * Additional tests that aim at testing more complex aggregation trees on larger random datasets, so that things like  * the growth of dynamic arrays is tested.  */
end_comment

begin_class
annotation|@
name|Slow
DECL|class|RandomTests
specifier|public
class|class
name|RandomTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
comment|// Make sure that unordered, reversed, disjoint and/or overlapping ranges are supported
comment|// Duel with filters
DECL|method|testRandomRanges
specifier|public
name|void
name|testRandomRanges
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|numDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|500
argument_list|,
literal|5000
argument_list|)
decl_stmt|;
specifier|final
name|double
index|[]
index|[]
name|docs
init|=
operator|new
name|double
index|[
name|numDocs
index|]
index|[]
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
name|numDocs
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|int
name|numValues
init|=
name|randomInt
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|docs
index|[
name|i
index|]
operator|=
operator|new
name|double
index|[
name|numValues
index|]
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numValues
condition|;
operator|++
name|j
control|)
block|{
name|docs
index|[
name|i
index|]
index|[
name|j
index|]
operator|=
name|randomDouble
argument_list|()
operator|*
literal|100
expr_stmt|;
block|}
block|}
name|createIndex
argument_list|(
literal|"idx"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|docs
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|XContentBuilder
name|source
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startArray
argument_list|(
literal|"values"
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|docs
index|[
name|i
index|]
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|source
operator|=
name|source
operator|.
name|value
argument_list|(
name|docs
index|[
name|i
index|]
index|[
name|j
index|]
argument_list|)
expr_stmt|;
block|}
name|source
operator|=
name|source
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"idx"
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|assertNoFailures
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|setIndicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|lenient
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numRanges
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
decl_stmt|;
specifier|final
name|double
index|[]
index|[]
name|ranges
init|=
operator|new
name|double
index|[
name|numRanges
index|]
index|[]
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
name|ranges
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
switch|switch
condition|(
name|randomInt
argument_list|(
literal|2
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
name|ranges
index|[
name|i
index|]
operator|=
operator|new
name|double
index|[]
block|{
name|Double
operator|.
name|NEGATIVE_INFINITY
block|,
name|randomInt
argument_list|(
literal|100
argument_list|)
block|}
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|ranges
index|[
name|i
index|]
operator|=
operator|new
name|double
index|[]
block|{
name|randomInt
argument_list|(
literal|100
argument_list|)
block|,
name|Double
operator|.
name|POSITIVE_INFINITY
block|}
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|ranges
index|[
name|i
index|]
operator|=
operator|new
name|double
index|[]
block|{
name|randomInt
argument_list|(
literal|100
argument_list|)
block|,
name|randomInt
argument_list|(
literal|100
argument_list|)
block|}
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
block|}
block|}
name|RangeBuilder
name|query
init|=
name|range
argument_list|(
literal|"range"
argument_list|)
operator|.
name|field
argument_list|(
literal|"values"
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
name|ranges
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|String
name|key
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|ranges
index|[
name|i
index|]
index|[
literal|0
index|]
operator|==
name|Double
operator|.
name|NEGATIVE_INFINITY
condition|)
block|{
name|query
operator|.
name|addUnboundedTo
argument_list|(
name|key
argument_list|,
name|ranges
index|[
name|i
index|]
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|ranges
index|[
name|i
index|]
index|[
literal|1
index|]
operator|==
name|Double
operator|.
name|POSITIVE_INFINITY
condition|)
block|{
name|query
operator|.
name|addUnboundedFrom
argument_list|(
name|key
argument_list|,
name|ranges
index|[
name|i
index|]
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|query
operator|.
name|addRange
argument_list|(
name|key
argument_list|,
name|ranges
index|[
name|i
index|]
index|[
literal|0
index|]
argument_list|,
name|ranges
index|[
name|i
index|]
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
name|SearchRequestBuilder
name|reqBuilder
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|query
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
name|ranges
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|RangeFilterBuilder
name|filter
init|=
name|FilterBuilders
operator|.
name|rangeFilter
argument_list|(
literal|"values"
argument_list|)
decl_stmt|;
if|if
condition|(
name|ranges
index|[
name|i
index|]
index|[
literal|0
index|]
operator|!=
name|Double
operator|.
name|NEGATIVE_INFINITY
condition|)
block|{
name|filter
operator|=
name|filter
operator|.
name|from
argument_list|(
name|ranges
index|[
name|i
index|]
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ranges
index|[
name|i
index|]
index|[
literal|1
index|]
operator|!=
name|Double
operator|.
name|POSITIVE_INFINITY
condition|)
block|{
name|filter
operator|=
name|filter
operator|.
name|to
argument_list|(
name|ranges
index|[
name|i
index|]
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
name|reqBuilder
operator|=
name|reqBuilder
operator|.
name|addAggregation
argument_list|(
name|filter
argument_list|(
literal|"filter"
operator|+
name|i
argument_list|)
operator|.
name|filter
argument_list|(
name|filter
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|SearchResponse
name|resp
init|=
name|reqBuilder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|Range
name|range
init|=
name|resp
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"range"
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
name|ranges
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|long
name|count
init|=
literal|0
decl_stmt|;
for|for
control|(
name|double
index|[]
name|values
range|:
name|docs
control|)
block|{
for|for
control|(
name|double
name|value
range|:
name|values
control|)
block|{
if|if
condition|(
name|value
operator|>=
name|ranges
index|[
name|i
index|]
index|[
literal|0
index|]
operator|&&
name|value
operator|<
name|ranges
index|[
name|i
index|]
index|[
literal|1
index|]
condition|)
block|{
operator|++
name|count
expr_stmt|;
break|break;
block|}
block|}
block|}
specifier|final
name|Range
operator|.
name|Bucket
name|bucket
init|=
name|range
operator|.
name|getBucketByKey
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|bucket
operator|.
name|getKey
argument_list|()
argument_list|,
name|count
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|Filter
name|filter
init|=
name|resp
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"filter"
operator|+
name|i
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|filter
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|count
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// test long/double/string terms aggs with high number of buckets that require array growth
DECL|method|testDuelTerms
specifier|public
name|void
name|testDuelTerms
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|numDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|1000
argument_list|,
literal|2000
argument_list|)
decl_stmt|;
specifier|final
name|int
name|maxNumTerms
init|=
name|randomIntBetween
argument_list|(
literal|10
argument_list|,
literal|5000
argument_list|)
decl_stmt|;
specifier|final
name|IntOpenHashSet
name|valuesSet
init|=
operator|new
name|IntOpenHashSet
argument_list|()
decl_stmt|;
name|immutableCluster
argument_list|()
operator|.
name|wipeIndices
argument_list|(
literal|"idx"
argument_list|)
expr_stmt|;
name|prepareCreate
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"string_values"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"not_analyzed"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"long_values"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"long"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"double_values"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"double"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|indexingRequests
init|=
name|Lists
operator|.
name|newArrayList
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
name|numDocs
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|int
index|[]
name|values
init|=
operator|new
name|int
index|[
name|randomInt
argument_list|(
literal|4
argument_list|)
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|values
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|values
index|[
name|j
index|]
operator|=
name|randomInt
argument_list|(
name|maxNumTerms
operator|-
literal|1
argument_list|)
operator|-
literal|1000
expr_stmt|;
name|valuesSet
operator|.
name|add
argument_list|(
name|values
index|[
name|j
index|]
argument_list|)
expr_stmt|;
block|}
name|XContentBuilder
name|source
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"num"
argument_list|,
name|randomDouble
argument_list|()
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"long_values"
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|values
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|source
operator|=
name|source
operator|.
name|value
argument_list|(
name|values
index|[
name|j
index|]
argument_list|)
expr_stmt|;
block|}
name|source
operator|=
name|source
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|(
literal|"double_values"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|values
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|source
operator|=
name|source
operator|.
name|value
argument_list|(
operator|(
name|double
operator|)
name|values
index|[
name|j
index|]
argument_list|)
expr_stmt|;
block|}
name|source
operator|=
name|source
operator|.
name|endArray
argument_list|()
operator|.
name|startArray
argument_list|(
literal|"string_values"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|values
operator|.
name|length
condition|;
operator|++
name|j
control|)
block|{
name|source
operator|=
name|source
operator|.
name|value
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|values
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|source
operator|=
name|source
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|indexingRequests
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"idx"
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|indexingRequests
argument_list|)
expr_stmt|;
name|assertNoFailures
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|setIndicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|lenient
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|SearchResponse
name|resp
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|terms
argument_list|(
literal|"long"
argument_list|)
operator|.
name|field
argument_list|(
literal|"long_values"
argument_list|)
operator|.
name|size
argument_list|(
name|maxNumTerms
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|min
argument_list|(
literal|"min"
argument_list|)
operator|.
name|field
argument_list|(
literal|"num"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|terms
argument_list|(
literal|"double"
argument_list|)
operator|.
name|field
argument_list|(
literal|"double_values"
argument_list|)
operator|.
name|size
argument_list|(
name|maxNumTerms
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|max
argument_list|(
literal|"max"
argument_list|)
operator|.
name|field
argument_list|(
literal|"num"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|terms
argument_list|(
literal|"string_map"
argument_list|)
operator|.
name|field
argument_list|(
literal|"string_values"
argument_list|)
operator|.
name|executionHint
argument_list|(
name|TermsAggregatorFactory
operator|.
name|ExecutionMode
operator|.
name|MAP
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|size
argument_list|(
name|maxNumTerms
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|stats
argument_list|(
literal|"stats"
argument_list|)
operator|.
name|field
argument_list|(
literal|"num"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|terms
argument_list|(
literal|"string_ordinals"
argument_list|)
operator|.
name|field
argument_list|(
literal|"string_values"
argument_list|)
operator|.
name|executionHint
argument_list|(
name|TermsAggregatorFactory
operator|.
name|ExecutionMode
operator|.
name|ORDINALS
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|size
argument_list|(
name|maxNumTerms
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|extendedStats
argument_list|(
literal|"stats"
argument_list|)
operator|.
name|field
argument_list|(
literal|"num"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertAllSuccessful
argument_list|(
name|resp
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|numDocs
argument_list|,
name|resp
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|Terms
name|longTerms
init|=
name|resp
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"long"
argument_list|)
decl_stmt|;
specifier|final
name|Terms
name|doubleTerms
init|=
name|resp
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"double"
argument_list|)
decl_stmt|;
specifier|final
name|Terms
name|stringMapTerms
init|=
name|resp
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"string_map"
argument_list|)
decl_stmt|;
specifier|final
name|Terms
name|stringOrdinalsTerms
init|=
name|resp
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"string_ordinals"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|valuesSet
operator|.
name|size
argument_list|()
argument_list|,
name|longTerms
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|valuesSet
operator|.
name|size
argument_list|()
argument_list|,
name|doubleTerms
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|valuesSet
operator|.
name|size
argument_list|()
argument_list|,
name|stringMapTerms
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|valuesSet
operator|.
name|size
argument_list|()
argument_list|,
name|stringOrdinalsTerms
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Terms
operator|.
name|Bucket
name|bucket
range|:
name|longTerms
operator|.
name|getBuckets
argument_list|()
control|)
block|{
specifier|final
name|Terms
operator|.
name|Bucket
name|doubleBucket
init|=
name|doubleTerms
operator|.
name|getBucketByKey
argument_list|(
name|Double
operator|.
name|toString
argument_list|(
name|Long
operator|.
name|parseLong
argument_list|(
name|bucket
operator|.
name|getKeyAsText
argument_list|()
operator|.
name|string
argument_list|()
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|Terms
operator|.
name|Bucket
name|stringMapBucket
init|=
name|stringMapTerms
operator|.
name|getBucketByKey
argument_list|(
name|bucket
operator|.
name|getKeyAsText
argument_list|()
operator|.
name|string
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|Terms
operator|.
name|Bucket
name|stringOrdinalsBucket
init|=
name|stringOrdinalsTerms
operator|.
name|getBucketByKey
argument_list|(
name|bucket
operator|.
name|getKeyAsText
argument_list|()
operator|.
name|string
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|doubleBucket
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|stringMapBucket
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|stringOrdinalsBucket
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|doubleBucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|stringMapBucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|stringOrdinalsBucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Duel between histograms and scripted terms
DECL|method|testDuelTermsHistogram
specifier|public
name|void
name|testDuelTermsHistogram
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"idx"
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|500
argument_list|,
literal|5000
argument_list|)
decl_stmt|;
specifier|final
name|int
name|maxNumTerms
init|=
name|randomIntBetween
argument_list|(
literal|10
argument_list|,
literal|2000
argument_list|)
decl_stmt|;
specifier|final
name|int
name|interval
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
specifier|final
name|Integer
index|[]
name|values
init|=
operator|new
name|Integer
index|[
name|maxNumTerms
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
name|values
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|values
index|[
name|i
index|]
operator|=
name|randomInt
argument_list|(
name|maxNumTerms
operator|*
literal|3
argument_list|)
operator|-
name|maxNumTerms
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numDocs
condition|;
operator|++
name|i
control|)
block|{
name|XContentBuilder
name|source
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"num"
argument_list|,
name|randomDouble
argument_list|()
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"values"
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numValues
init|=
name|randomInt
argument_list|(
literal|4
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numValues
condition|;
operator|++
name|j
control|)
block|{
name|source
operator|=
name|source
operator|.
name|value
argument_list|(
name|randomFrom
argument_list|(
name|values
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|source
operator|=
name|source
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"idx"
argument_list|,
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|assertNoFailures
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|setIndicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|lenient
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|SearchResponse
name|resp
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|terms
argument_list|(
literal|"terms"
argument_list|)
operator|.
name|field
argument_list|(
literal|"values"
argument_list|)
operator|.
name|script
argument_list|(
literal|"floor(_value / interval)"
argument_list|)
operator|.
name|param
argument_list|(
literal|"interval"
argument_list|,
name|interval
argument_list|)
operator|.
name|size
argument_list|(
name|maxNumTerms
argument_list|)
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|histogram
argument_list|(
literal|"histo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"values"
argument_list|)
operator|.
name|interval
argument_list|(
name|interval
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
name|resp
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Terms
name|terms
init|=
name|resp
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"terms"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|terms
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Histogram
name|histo
init|=
name|resp
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"histo"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|histo
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|histo
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|Terms
operator|.
name|Bucket
name|bucket
range|:
name|terms
operator|.
name|getBuckets
argument_list|()
control|)
block|{
specifier|final
name|long
name|key
init|=
name|bucket
operator|.
name|getKeyAsNumber
argument_list|()
operator|.
name|longValue
argument_list|()
operator|*
name|interval
decl_stmt|;
specifier|final
name|Histogram
operator|.
name|Bucket
name|histoBucket
init|=
name|histo
operator|.
name|getBucketByKey
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|histoBucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testLargeNumbersOfPercentileBuckets
specifier|public
name|void
name|testLargeNumbersOfPercentileBuckets
parameter_list|()
throws|throws
name|Exception
block|{
comment|// test high numbers of percentile buckets to make sure paging and release work correctly
name|createIndex
argument_list|(
literal|"idx"
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|2500
argument_list|,
literal|5000
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Indexing ["
operator|+
name|numDocs
operator|+
literal|"] docs"
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|indexingRequests
init|=
name|Lists
operator|.
name|newArrayList
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
name|numDocs
condition|;
operator|++
name|i
control|)
block|{
name|indexingRequests
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"idx"
argument_list|,
literal|"type"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"double_value"
argument_list|,
name|randomDouble
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|indexingRequests
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
literal|"idx"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|terms
argument_list|(
literal|"terms"
argument_list|)
operator|.
name|field
argument_list|(
literal|"double_value"
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|percentiles
argument_list|(
literal|"pcts"
argument_list|)
operator|.
name|field
argument_list|(
literal|"double_value"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertAllSuccessful
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|numDocs
argument_list|,
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

