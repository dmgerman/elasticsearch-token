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
name|ImmutableSettings
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
name|missing
operator|.
name|Missing
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
name|avg
operator|.
name|Avg
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
DECL|class|MissingTests
specifier|public
class|class
name|MissingTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Override
DECL|method|indexSettings
specifier|public
name|Settings
name|indexSettings
parameter_list|()
block|{
return|return
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
name|between
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
name|between
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|field|numDocs
DECL|field|numDocsMissing
DECL|field|numDocsUnmapped
name|int
name|numDocs
decl_stmt|,
name|numDocsMissing
decl_stmt|,
name|numDocsUnmapped
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
name|createIndex
argument_list|(
literal|"idx"
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
argument_list|<
name|IndexRequestBuilder
argument_list|>
argument_list|()
decl_stmt|;
name|numDocs
operator|=
name|randomIntBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
expr_stmt|;
name|numDocsMissing
operator|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|numDocs
operator|-
literal|1
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
name|numDocsMissing
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
literal|"idx"
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
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
name|numDocsMissing
init|;
name|i
operator|<
name|numDocs
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
literal|"idx"
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
literal|"tag"
argument_list|,
literal|"tag1"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|createIndex
argument_list|(
literal|"unmapped_idx"
argument_list|)
expr_stmt|;
name|numDocsUnmapped
operator|=
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|5
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
name|numDocsUnmapped
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
literal|"unmapped_idx"
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
argument_list|)
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
name|ensureGreen
argument_list|()
expr_stmt|;
comment|// wait until we are ready to serve requests
name|ensureSearchable
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|unmapped
specifier|public
name|void
name|unmapped
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
literal|"unmapped_idx"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|missing
argument_list|(
literal|"missing_tag"
argument_list|)
operator|.
name|field
argument_list|(
literal|"tag"
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
name|Missing
name|missing
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"missing_tag"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|missing
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|missing
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"missing_tag"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|missing
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numDocsUnmapped
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|partiallyUnmapped
specifier|public
name|void
name|partiallyUnmapped
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
argument_list|,
literal|"unmapped_idx"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|missing
argument_list|(
literal|"missing_tag"
argument_list|)
operator|.
name|field
argument_list|(
literal|"tag"
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
name|Missing
name|missing
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"missing_tag"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|missing
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|missing
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"missing_tag"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|missing
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numDocsMissing
operator|+
name|numDocsUnmapped
argument_list|)
argument_list|)
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
name|missing
argument_list|(
literal|"missing_tag"
argument_list|)
operator|.
name|field
argument_list|(
literal|"tag"
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
name|Missing
name|missing
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"missing_tag"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|missing
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|missing
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"missing_tag"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|missing
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numDocsMissing
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|withSubAggregation
specifier|public
name|void
name|withSubAggregation
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
argument_list|,
literal|"unmapped_idx"
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|missing
argument_list|(
literal|"missing_tag"
argument_list|)
operator|.
name|field
argument_list|(
literal|"tag"
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|avg
argument_list|(
literal|"avg_value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
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
name|assertThat
argument_list|(
literal|"Not all shards are initialized"
argument_list|,
name|response
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|response
operator|.
name|getTotalShards
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Missing
name|missing
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"missing_tag"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|missing
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|missing
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"missing_tag"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|missing
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numDocsMissing
operator|+
name|numDocsUnmapped
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|missing
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
name|long
name|sum
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
name|numDocsMissing
condition|;
operator|++
name|i
control|)
block|{
name|sum
operator|+=
name|i
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
name|numDocsUnmapped
condition|;
operator|++
name|i
control|)
block|{
name|sum
operator|+=
name|i
expr_stmt|;
block|}
name|Avg
name|avgValue
init|=
name|missing
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg_value"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avgValue
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avgValue
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg_value"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avgValue
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|double
operator|)
name|sum
operator|/
operator|(
name|numDocsMissing
operator|+
name|numDocsUnmapped
operator|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|withInheritedSubMissing
specifier|public
name|void
name|withInheritedSubMissing
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
argument_list|()
operator|.
name|addAggregation
argument_list|(
name|missing
argument_list|(
literal|"top_missing"
argument_list|)
operator|.
name|field
argument_list|(
literal|"tag"
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|missing
argument_list|(
literal|"sub_missing"
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
name|Missing
name|topMissing
init|=
name|response
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"top_missing"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|topMissing
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topMissing
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"top_missing"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topMissing
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numDocsMissing
operator|+
name|numDocsUnmapped
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topMissing
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
name|Missing
name|subMissing
init|=
name|topMissing
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"sub_missing"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|subMissing
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|subMissing
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"sub_missing"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|subMissing
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numDocsMissing
operator|+
name|numDocsUnmapped
argument_list|)
argument_list|)
expr_stmt|;
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
argument_list|<
name|IndexRequestBuilder
argument_list|>
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
name|emptyBuckets
argument_list|(
literal|true
argument_list|)
operator|.
name|subAggregation
argument_list|(
name|missing
argument_list|(
literal|"missing"
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
name|getByKey
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
name|Missing
name|missing
init|=
name|bucket
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"missing"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|missing
argument_list|,
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|missing
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"missing"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|missing
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

