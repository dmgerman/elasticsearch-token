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
name|junit
operator|.
name|annotations
operator|.
name|TestLogging
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
name|avg
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
name|histogram
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
name|*
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AvgTests
specifier|public
class|class
name|AvgTests
extends|extends
name|AbstractNumericTests
block|{
annotation|@
name|Test
DECL|method|testEmptyAggregation
specifier|public
name|void
name|testEmptyAggregation
parameter_list|()
throws|throws
name|Exception
block|{
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
name|avg
argument_list|(
literal|"avg"
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
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|bucket
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|Double
operator|.
name|isNaN
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testUnmapped
specifier|public
name|void
name|testUnmapped
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx_unmapped"
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
name|avg
argument_list|(
literal|"avg"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
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
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Double
operator|.
name|NaN
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSingleValuedField
specifier|public
name|void
name|testSingleValuedField
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|avg
argument_list|(
literal|"avg"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
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
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
call|(
name|double
call|)
argument_list|(
literal|1
operator|+
literal|2
operator|+
literal|3
operator|+
literal|4
operator|+
literal|5
operator|+
literal|6
operator|+
literal|7
operator|+
literal|8
operator|+
literal|9
operator|+
literal|10
argument_list|)
operator|/
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testSingleValuedField_PartiallyUnmapped
specifier|public
name|void
name|testSingleValuedField_PartiallyUnmapped
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|,
literal|"idx_unmapped"
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
name|avg
argument_list|(
literal|"avg"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
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
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
call|(
name|double
call|)
argument_list|(
literal|1
operator|+
literal|2
operator|+
literal|3
operator|+
literal|4
operator|+
literal|5
operator|+
literal|6
operator|+
literal|7
operator|+
literal|8
operator|+
literal|9
operator|+
literal|10
argument_list|)
operator|/
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSingleValuedField_WithValueScript
specifier|public
name|void
name|testSingleValuedField_WithValueScript
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|avg
argument_list|(
literal|"avg"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
argument_list|)
operator|.
name|script
argument_list|(
literal|"_value + 1"
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
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
call|(
name|double
call|)
argument_list|(
literal|2
operator|+
literal|3
operator|+
literal|4
operator|+
literal|5
operator|+
literal|6
operator|+
literal|7
operator|+
literal|8
operator|+
literal|9
operator|+
literal|10
operator|+
literal|11
argument_list|)
operator|/
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSingleValuedField_WithValueScript_WithParams
specifier|public
name|void
name|testSingleValuedField_WithValueScript_WithParams
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|avg
argument_list|(
literal|"avg"
argument_list|)
operator|.
name|field
argument_list|(
literal|"value"
argument_list|)
operator|.
name|script
argument_list|(
literal|"_value + inc"
argument_list|)
operator|.
name|param
argument_list|(
literal|"inc"
argument_list|,
literal|1
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
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
call|(
name|double
call|)
argument_list|(
literal|2
operator|+
literal|3
operator|+
literal|4
operator|+
literal|5
operator|+
literal|6
operator|+
literal|7
operator|+
literal|8
operator|+
literal|9
operator|+
literal|10
operator|+
literal|11
argument_list|)
operator|/
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMultiValuedField
specifier|public
name|void
name|testMultiValuedField
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|avg
argument_list|(
literal|"avg"
argument_list|)
operator|.
name|field
argument_list|(
literal|"values"
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
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
call|(
name|double
call|)
argument_list|(
literal|2
operator|+
literal|3
operator|+
literal|3
operator|+
literal|4
operator|+
literal|4
operator|+
literal|5
operator|+
literal|5
operator|+
literal|6
operator|+
literal|6
operator|+
literal|7
operator|+
literal|7
operator|+
literal|8
operator|+
literal|8
operator|+
literal|9
operator|+
literal|9
operator|+
literal|10
operator|+
literal|10
operator|+
literal|11
operator|+
literal|11
operator|+
literal|12
argument_list|)
operator|/
literal|20
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMultiValuedField_WithValueScript
specifier|public
name|void
name|testMultiValuedField_WithValueScript
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|avg
argument_list|(
literal|"avg"
argument_list|)
operator|.
name|field
argument_list|(
literal|"values"
argument_list|)
operator|.
name|script
argument_list|(
literal|"_value + 1"
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
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
call|(
name|double
call|)
argument_list|(
literal|3
operator|+
literal|4
operator|+
literal|4
operator|+
literal|5
operator|+
literal|5
operator|+
literal|6
operator|+
literal|6
operator|+
literal|7
operator|+
literal|7
operator|+
literal|8
operator|+
literal|8
operator|+
literal|9
operator|+
literal|9
operator|+
literal|10
operator|+
literal|10
operator|+
literal|11
operator|+
literal|11
operator|+
literal|12
operator|+
literal|12
operator|+
literal|13
argument_list|)
operator|/
literal|20
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMultiValuedField_WithValueScript_WithParams
specifier|public
name|void
name|testMultiValuedField_WithValueScript_WithParams
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|avg
argument_list|(
literal|"avg"
argument_list|)
operator|.
name|field
argument_list|(
literal|"values"
argument_list|)
operator|.
name|script
argument_list|(
literal|"_value + inc"
argument_list|)
operator|.
name|param
argument_list|(
literal|"inc"
argument_list|,
literal|1
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
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
call|(
name|double
call|)
argument_list|(
literal|3
operator|+
literal|4
operator|+
literal|4
operator|+
literal|5
operator|+
literal|5
operator|+
literal|6
operator|+
literal|6
operator|+
literal|7
operator|+
literal|7
operator|+
literal|8
operator|+
literal|8
operator|+
literal|9
operator|+
literal|9
operator|+
literal|10
operator|+
literal|10
operator|+
literal|11
operator|+
literal|11
operator|+
literal|12
operator|+
literal|12
operator|+
literal|13
argument_list|)
operator|/
literal|20
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testScript_SingleValued
specifier|public
name|void
name|testScript_SingleValued
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|avg
argument_list|(
literal|"avg"
argument_list|)
operator|.
name|script
argument_list|(
literal|"doc['value'].value"
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
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
call|(
name|double
call|)
argument_list|(
literal|1
operator|+
literal|2
operator|+
literal|3
operator|+
literal|4
operator|+
literal|5
operator|+
literal|6
operator|+
literal|7
operator|+
literal|8
operator|+
literal|9
operator|+
literal|10
argument_list|)
operator|/
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testScript_SingleValued_WithParams
specifier|public
name|void
name|testScript_SingleValued_WithParams
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|avg
argument_list|(
literal|"avg"
argument_list|)
operator|.
name|script
argument_list|(
literal|"doc['value'].value + inc"
argument_list|)
operator|.
name|param
argument_list|(
literal|"inc"
argument_list|,
literal|1
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
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
call|(
name|double
call|)
argument_list|(
literal|2
operator|+
literal|3
operator|+
literal|4
operator|+
literal|5
operator|+
literal|6
operator|+
literal|7
operator|+
literal|8
operator|+
literal|9
operator|+
literal|10
operator|+
literal|11
argument_list|)
operator|/
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testScript_ExplicitSingleValued_WithParams
specifier|public
name|void
name|testScript_ExplicitSingleValued_WithParams
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|avg
argument_list|(
literal|"avg"
argument_list|)
operator|.
name|script
argument_list|(
literal|"doc['value'].value + inc"
argument_list|)
operator|.
name|param
argument_list|(
literal|"inc"
argument_list|,
literal|1
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
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
call|(
name|double
call|)
argument_list|(
literal|2
operator|+
literal|3
operator|+
literal|4
operator|+
literal|5
operator|+
literal|6
operator|+
literal|7
operator|+
literal|8
operator|+
literal|9
operator|+
literal|10
operator|+
literal|11
argument_list|)
operator|/
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testScript_MultiValued
specifier|public
name|void
name|testScript_MultiValued
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|avg
argument_list|(
literal|"avg"
argument_list|)
operator|.
name|script
argument_list|(
literal|"new double[] { doc['value'].value, doc['value'].value + 1 }"
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
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
call|(
name|double
call|)
argument_list|(
literal|1
operator|+
literal|2
operator|+
literal|2
operator|+
literal|3
operator|+
literal|3
operator|+
literal|4
operator|+
literal|4
operator|+
literal|5
operator|+
literal|5
operator|+
literal|6
operator|+
literal|6
operator|+
literal|7
operator|+
literal|7
operator|+
literal|8
operator|+
literal|8
operator|+
literal|9
operator|+
literal|9
operator|+
literal|10
operator|+
literal|10
operator|+
literal|11
argument_list|)
operator|/
literal|20
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testScript_ExplicitMultiValued
specifier|public
name|void
name|testScript_ExplicitMultiValued
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|avg
argument_list|(
literal|"avg"
argument_list|)
operator|.
name|script
argument_list|(
literal|"new double[] { doc['value'].value, doc['value'].value + 1 }"
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
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
call|(
name|double
call|)
argument_list|(
literal|1
operator|+
literal|2
operator|+
literal|2
operator|+
literal|3
operator|+
literal|3
operator|+
literal|4
operator|+
literal|4
operator|+
literal|5
operator|+
literal|5
operator|+
literal|6
operator|+
literal|6
operator|+
literal|7
operator|+
literal|7
operator|+
literal|8
operator|+
literal|8
operator|+
literal|9
operator|+
literal|9
operator|+
literal|10
operator|+
literal|10
operator|+
literal|11
argument_list|)
operator|/
literal|20
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
annotation|@
name|TestLogging
argument_list|(
literal|"search:TRACE"
argument_list|)
DECL|method|testScript_MultiValued_WithParams
specifier|public
name|void
name|testScript_MultiValued_WithParams
parameter_list|()
throws|throws
name|Exception
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
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
name|avg
argument_list|(
literal|"avg"
argument_list|)
operator|.
name|script
argument_list|(
literal|"new double[] { doc['value'].value, doc['value'].value + inc }"
argument_list|)
operator|.
name|param
argument_list|(
literal|"inc"
argument_list|,
literal|1
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
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
name|Avg
name|avg
init|=
name|searchResponse
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"avg"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|avg
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getName
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"avg"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|avg
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
call|(
name|double
call|)
argument_list|(
literal|1
operator|+
literal|2
operator|+
literal|2
operator|+
literal|3
operator|+
literal|3
operator|+
literal|4
operator|+
literal|4
operator|+
literal|5
operator|+
literal|5
operator|+
literal|6
operator|+
literal|6
operator|+
literal|7
operator|+
literal|7
operator|+
literal|8
operator|+
literal|8
operator|+
literal|9
operator|+
literal|9
operator|+
literal|10
operator|+
literal|10
operator|+
literal|11
argument_list|)
operator|/
literal|20
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

