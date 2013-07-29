begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.flt
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|flt
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
name|SearchPhaseExecutionException
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
name|test
operator|.
name|integration
operator|.
name|AbstractSharedClusterTest
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
name|fuzzyLikeThisFieldQuery
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
name|fuzzyLikeThisQuery
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
name|assertThrows
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|FuzzyLikeThisActionTests
specifier|public
class|class
name|FuzzyLikeThisActionTests
extends|extends
name|AbstractSharedClusterTest
block|{
annotation|@
name|Test
comment|// See issue https://github.com/elasticsearch/elasticsearch/issues/3252
DECL|method|testNumericField
specifier|public
name|void
name|testNumericField
parameter_list|()
throws|throws
name|Exception
block|{
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
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
literal|"string_value"
argument_list|,
literal|"lucene index"
argument_list|)
operator|.
name|field
argument_list|(
literal|"int_value"
argument_list|,
literal|1
argument_list|)
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
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"2"
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
literal|"string_value"
argument_list|,
literal|"elasticsearch index"
argument_list|)
operator|.
name|field
argument_list|(
literal|"int_value"
argument_list|,
literal|42
argument_list|)
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
name|refresh
argument_list|()
expr_stmt|;
comment|// flt query with no field -> OK
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|fuzzyLikeThisQuery
argument_list|()
operator|.
name|likeText
argument_list|(
literal|"index"
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
name|getFailedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
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
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
comment|// flt query with string fields
name|searchResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|fuzzyLikeThisQuery
argument_list|(
literal|"string_value"
argument_list|)
operator|.
name|likeText
argument_list|(
literal|"index"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
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
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
comment|// flt query with at least a numeric field -> fail by default
name|assertThrows
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|fuzzyLikeThisQuery
argument_list|(
literal|"string_value"
argument_list|,
literal|"int_value"
argument_list|)
operator|.
name|likeText
argument_list|(
literal|"index"
argument_list|)
argument_list|)
argument_list|,
name|SearchPhaseExecutionException
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// flt query with at least a numeric field -> fail by command
name|assertThrows
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|fuzzyLikeThisQuery
argument_list|(
literal|"string_value"
argument_list|,
literal|"int_value"
argument_list|)
operator|.
name|likeText
argument_list|(
literal|"index"
argument_list|)
operator|.
name|failOnUnsupportedField
argument_list|(
literal|true
argument_list|)
argument_list|)
argument_list|,
name|SearchPhaseExecutionException
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// flt query with at least a numeric field but fail_on_unsupported_field set to false
name|searchResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|fuzzyLikeThisQuery
argument_list|(
literal|"string_value"
argument_list|,
literal|"int_value"
argument_list|)
operator|.
name|likeText
argument_list|(
literal|"index"
argument_list|)
operator|.
name|failOnUnsupportedField
argument_list|(
literal|false
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
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
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
comment|// flt field query on a numeric field -> failure by default
name|assertThrows
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|fuzzyLikeThisFieldQuery
argument_list|(
literal|"int_value"
argument_list|)
operator|.
name|likeText
argument_list|(
literal|"42"
argument_list|)
argument_list|)
argument_list|,
name|SearchPhaseExecutionException
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// flt field query on a numeric field -> failure by command
name|assertThrows
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|fuzzyLikeThisFieldQuery
argument_list|(
literal|"int_value"
argument_list|)
operator|.
name|likeText
argument_list|(
literal|"42"
argument_list|)
operator|.
name|failOnUnsupportedField
argument_list|(
literal|true
argument_list|)
argument_list|)
argument_list|,
name|SearchPhaseExecutionException
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// flt field query on a numeric field but fail_on_unsupported_field set to false
name|searchResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|fuzzyLikeThisFieldQuery
argument_list|(
literal|"int_value"
argument_list|)
operator|.
name|likeText
argument_list|(
literal|"42"
argument_list|)
operator|.
name|failOnUnsupportedField
argument_list|(
literal|false
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
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
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

