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
name|ElasticsearchException
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
name|nested
operator|.
name|Nested
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
name|LongTerms
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
operator|.
name|Bucket
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
name|max
operator|.
name|Max
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
name|Stats
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
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|cache
operator|.
name|recycler
operator|.
name|MockBigArrays
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
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
name|org
operator|.
name|junit
operator|.
name|Test
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
name|assertAcked
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
name|assertSearchResponse
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
name|Matchers
operator|.
name|is
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
comment|/**  *  */
end_comment

begin_class
DECL|class|NestedTests
specifier|public
class|class
name|NestedTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|field|numParents
name|int
name|numParents
decl_stmt|;
DECL|field|numChildren
name|int
index|[]
name|numChildren
decl_stmt|;
annotation|@
name|Before
DECL|method|init
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
literal|"nested"
argument_list|,
literal|"type=nested"
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|builders
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|numParents
operator|=
name|randomIntBetween
argument_list|(
literal|3
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|numChildren
operator|=
operator|new
name|int
index|[
name|numParents
index|]
expr_stmt|;
name|int
name|totalChildren
init|=
literal|0
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
name|numParents
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|i
operator|==
name|numParents
operator|-
literal|1
operator|&&
name|totalChildren
operator|==
literal|0
condition|)
block|{
comment|// we need at least one child overall
name|numChildren
index|[
name|i
index|]
operator|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|numChildren
index|[
name|i
index|]
operator|=
name|randomInt
argument_list|(
literal|5
argument_list|)
expr_stmt|;
block|}
name|totalChildren
operator|+=
name|numChildren
index|[
name|i
index|]
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|totalChildren
operator|>
literal|0
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
name|numParents
condition|;
name|i
operator|++
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
literal|"value"
argument_list|,
name|i
operator|+
literal|1
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"nested"
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
name|numChildren
index|[
name|i
index|]
condition|;
operator|++
name|j
control|)
block|{
name|source
operator|=
name|source
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"value"
argument_list|,
name|i
operator|+
literal|1
operator|+
name|j
argument_list|)
operator|.
name|endObject
argument_list|()
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
name|builders
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
literal|""
operator|+
name|i
operator|+
literal|1
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
name|builders
argument_list|)
expr_stmt|;
name|ensureSearchable
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|simple
specifier|public
name|void
name|simple
parameter_list|()
throws|throws
name|Exception
block|{
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
name|nested
argument_list|(
literal|"nested"
argument_list|)
operator|.
name|path
argument_list|(
literal|"nested"
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|stats
argument_list|(
literal|"nested_value_stats"
argument_list|)
operator|.
name|field
argument_list|(
literal|"nested.value"
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
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|double
name|min
init|=
name|Double
operator|.
name|POSITIVE_INFINITY
decl_stmt|;
name|double
name|max
init|=
name|Double
operator|.
name|NEGATIVE_INFINITY
decl_stmt|;
name|long
name|sum
init|=
literal|0
decl_stmt|;
name|long
name|count
init|=
literal|0
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
name|numParents
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numChildren
index|[
name|i
index|]
condition|;
operator|++
name|j
control|)
block|{
specifier|final
name|long
name|value
init|=
name|i
operator|+
literal|1
operator|+
name|j
decl_stmt|;
name|min
operator|=
name|Math
operator|.
name|min
argument_list|(
name|min
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|max
operator|=
name|Math
operator|.
name|max
argument_list|(
name|max
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|sum
operator|+=
name|value
expr_stmt|;
operator|++
name|count
expr_stmt|;
block|}
block|}
name|Nested
name|nested
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"nested"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|nested
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nested
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"nested"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nested
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
name|assertThat
argument_list|(
name|nested
operator|.
name|getAggregations
argument_list|()
operator|.
name|asList
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|Stats
name|stats
init|=
name|nested
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"nested_value_stats"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|stats
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|getMin
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|min
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|getMax
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|max
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|count
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|getSum
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|double
operator|)
name|sum
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stats
operator|.
name|getAvg
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|double
operator|)
name|sum
operator|/
name|count
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|onNonNestedField
specifier|public
name|void
name|onNonNestedField
parameter_list|()
throws|throws
name|Exception
block|{
name|MockBigArrays
operator|.
name|discardNextCheck
argument_list|()
expr_stmt|;
try|try
block|{
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
name|nested
argument_list|(
literal|"nested"
argument_list|)
operator|.
name|path
argument_list|(
literal|"value"
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|stats
argument_list|(
literal|"nested_value_stats"
argument_list|)
operator|.
name|field
argument_list|(
literal|"nested.value"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"expected execution to fail - an attempt to nested facet on non-nested field/path"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchException
name|ese
parameter_list|)
block|{         }
block|}
annotation|@
name|Test
DECL|method|nestedWithSubTermsAgg
specifier|public
name|void
name|nestedWithSubTermsAgg
parameter_list|()
throws|throws
name|Exception
block|{
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
name|nested
argument_list|(
literal|"nested"
argument_list|)
operator|.
name|path
argument_list|(
literal|"nested"
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|terms
argument_list|(
literal|"values"
argument_list|)
operator|.
name|field
argument_list|(
literal|"nested.value"
argument_list|)
operator|.
name|size
argument_list|(
literal|100
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
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|long
name|docCount
init|=
literal|0
decl_stmt|;
name|long
index|[]
name|counts
init|=
operator|new
name|long
index|[
name|numParents
operator|+
literal|6
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
name|numParents
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numChildren
index|[
name|i
index|]
condition|;
operator|++
name|j
control|)
block|{
specifier|final
name|int
name|value
init|=
name|i
operator|+
literal|1
operator|+
name|j
decl_stmt|;
operator|++
name|counts
index|[
name|value
index|]
expr_stmt|;
operator|++
name|docCount
expr_stmt|;
block|}
block|}
name|int
name|uniqueValues
init|=
literal|0
decl_stmt|;
for|for
control|(
name|long
name|count
range|:
name|counts
control|)
block|{
if|if
condition|(
name|count
operator|>
literal|0
condition|)
block|{
operator|++
name|uniqueValues
expr_stmt|;
block|}
block|}
name|Nested
name|nested
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"nested"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|nested
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nested
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"nested"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nested
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|docCount
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nested
operator|.
name|getAggregations
argument_list|()
operator|.
name|asList
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|LongTerms
name|values
init|=
name|nested
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"values"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|values
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"values"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|getBuckets
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|uniqueValues
argument_list|)
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
name|counts
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|String
name|key
init|=
name|Long
operator|.
name|toString
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|counts
index|[
name|i
index|]
operator|==
literal|0
condition|)
block|{
name|assertNull
argument_list|(
name|values
operator|.
name|getBucketByKey
argument_list|(
name|key
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|Bucket
name|bucket
init|=
name|values
operator|.
name|getBucketByKey
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|bucket
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|counts
index|[
name|i
index|]
argument_list|,
name|bucket
operator|.
name|getDocCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
DECL|method|nestedAsSubAggregation
specifier|public
name|void
name|nestedAsSubAggregation
parameter_list|()
throws|throws
name|Exception
block|{
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
literal|"top_values"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
argument_list|)
operator|.
name|size
argument_list|(
literal|100
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|nested
argument_list|(
literal|"nested"
argument_list|)
operator|.
name|path
argument_list|(
literal|"nested"
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|max
argument_list|(
literal|"max_value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"nested.value"
argument_list|)
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
name|assertSearchResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|LongTerms
name|values
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"top_values"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|values
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"top_values"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|getBuckets
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numParents
argument_list|)
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
name|numParents
condition|;
name|i
operator|++
control|)
block|{
name|String
name|topValue
init|=
literal|""
operator|+
operator|(
name|i
operator|+
literal|1
operator|)
decl_stmt|;
name|assertThat
argument_list|(
name|values
operator|.
name|getBucketByKey
argument_list|(
name|topValue
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Nested
name|nested
init|=
name|values
operator|.
name|getBucketByKey
argument_list|(
name|topValue
argument_list|)
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"nested"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|nested
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Max
name|max
init|=
name|nested
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"max_value"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|max
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|max
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numChildren
index|[
name|i
index|]
operator|==
literal|0
condition|?
name|Double
operator|.
name|NEGATIVE_INFINITY
else|:
operator|(
name|double
operator|)
name|i
operator|+
name|numChildren
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|emptyAggregation
specifier|public
name|void
name|emptyAggregation
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareCreate
argument_list|(
literal|"empty_bucket_idx"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type"
argument_list|,
literal|"value"
argument_list|,
literal|"type=integer"
argument_list|,
literal|"nested"
argument_list|,
literal|"type=nested"
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
name|builders
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|builders
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"empty_bucket_idx"
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
literal|"value"
argument_list|,
name|i
operator|*
literal|2
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"nested"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"value"
argument_list|,
name|i
operator|+
literal|1
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"value"
argument_list|,
name|i
operator|+
literal|2
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"value"
argument_list|,
name|i
operator|+
literal|3
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"value"
argument_list|,
name|i
operator|+
literal|4
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"value"
argument_list|,
name|i
operator|+
literal|5
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|builders
operator|.
name|toArray
argument_list|(
operator|new
name|IndexRequestBuilder
index|[
name|builders
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"empty_bucket_idx"
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
name|histogram
argument_list|(
literal|"histo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
argument_list|)
operator|.
name|interval
argument_list|(
literal|1l
argument_list|)
operator|.
name|minDocCount
argument_list|(
literal|0
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|nested
argument_list|(
literal|"nested"
argument_list|)
operator|.
name|path
argument_list|(
literal|"nested"
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
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|Histogram
name|histo
init|=
name|searchResponse
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
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Histogram
operator|.
name|Bucket
name|bucket
init|=
name|histo
operator|.
name|getBucketByKey
argument_list|(
literal|1l
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|bucket
argument_list|,
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Nested
name|nested
init|=
name|bucket
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"nested"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|nested
argument_list|,
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nested
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"nested"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nested
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|is
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

