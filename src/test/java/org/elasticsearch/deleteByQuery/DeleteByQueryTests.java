begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.deleteByQuery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|deleteByQuery
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
name|ShardOperationFailedException
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
name|admin
operator|.
name|indices
operator|.
name|alias
operator|.
name|Alias
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
name|deletebyquery
operator|.
name|DeleteByQueryRequestBuilder
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
name|deletebyquery
operator|.
name|DeleteByQueryResponse
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
name|deletebyquery
operator|.
name|IndexDeleteByQueryResponse
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
name|index
operator|.
name|query
operator|.
name|QueryBuilders
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|IndexMissingException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
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
name|assertHitCount
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
name|*
import|;
end_import

begin_class
DECL|class|DeleteByQueryTests
specifier|public
class|class
name|DeleteByQueryTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testDeleteAllNoIndices
specifier|public
name|void
name|testDeleteAllNoIndices
parameter_list|()
block|{
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
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|DeleteByQueryRequestBuilder
name|deleteByQueryRequestBuilder
init|=
name|client
argument_list|()
operator|.
name|prepareDeleteByQuery
argument_list|()
decl_stmt|;
name|deleteByQueryRequestBuilder
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
expr_stmt|;
name|deleteByQueryRequestBuilder
operator|.
name|setIndicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|DeleteByQueryResponse
name|actionGet
init|=
name|deleteByQueryRequestBuilder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|actionGet
operator|.
name|getIndices
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testDeleteAllOneIndex
specifier|public
name|void
name|testDeleteAllOneIndex
parameter_list|()
block|{
name|String
name|json
init|=
literal|"{"
operator|+
literal|"\"user\":\"kimchy\","
operator|+
literal|"\"postDate\":\"2013-01-30\","
operator|+
literal|"\"message\":\"trying out Elastic Search\""
operator|+
literal|"}"
decl_stmt|;
specifier|final
name|long
name|iters
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|50
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
name|iters
condition|;
name|i
operator|++
control|)
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"twitter"
argument_list|,
literal|"tweet"
argument_list|,
literal|""
operator|+
name|i
argument_list|)
operator|.
name|setSource
argument_list|(
name|json
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|refresh
argument_list|()
expr_stmt|;
name|SearchResponse
name|search
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
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
name|search
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|iters
argument_list|)
argument_list|)
expr_stmt|;
name|DeleteByQueryRequestBuilder
name|deleteByQueryRequestBuilder
init|=
name|client
argument_list|()
operator|.
name|prepareDeleteByQuery
argument_list|()
decl_stmt|;
name|deleteByQueryRequestBuilder
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
expr_stmt|;
name|DeleteByQueryResponse
name|actionGet
init|=
name|deleteByQueryRequestBuilder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|actionGet
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|OK
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actionGet
operator|.
name|getIndex
argument_list|(
literal|"twitter"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actionGet
operator|.
name|getIndex
argument_list|(
literal|"twitter"
argument_list|)
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
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|search
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
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
name|search
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMissing
specifier|public
name|void
name|testMissing
parameter_list|()
block|{
name|String
name|json
init|=
literal|"{"
operator|+
literal|"\"user\":\"kimchy\","
operator|+
literal|"\"postDate\":\"2013-01-30\","
operator|+
literal|"\"message\":\"trying out Elastic Search\""
operator|+
literal|"}"
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"twitter"
argument_list|,
literal|"tweet"
argument_list|)
operator|.
name|setSource
argument_list|(
name|json
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|SearchResponse
name|search
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
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
name|search
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|DeleteByQueryRequestBuilder
name|deleteByQueryRequestBuilder
init|=
name|client
argument_list|()
operator|.
name|prepareDeleteByQuery
argument_list|()
decl_stmt|;
name|deleteByQueryRequestBuilder
operator|.
name|setIndices
argument_list|(
literal|"twitter"
argument_list|,
literal|"missing"
argument_list|)
expr_stmt|;
name|deleteByQueryRequestBuilder
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|deleteByQueryRequestBuilder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Exception should have been thrown."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IndexMissingException
name|e
parameter_list|)
block|{
comment|//everything well
block|}
name|deleteByQueryRequestBuilder
operator|.
name|setIndicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|lenientExpandOpen
argument_list|()
argument_list|)
expr_stmt|;
name|DeleteByQueryResponse
name|actionGet
init|=
name|deleteByQueryRequestBuilder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|actionGet
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|OK
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|actionGet
operator|.
name|getIndex
argument_list|(
literal|"twitter"
argument_list|)
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
name|actionGet
operator|.
name|getIndex
argument_list|(
literal|"twitter"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
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
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|search
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
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
name|search
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testFailure
specifier|public
name|void
name|testFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addAlias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"alias"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|DeleteByQueryResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareDeleteByQuery
argument_list|(
name|indexOrAlias
argument_list|()
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|hasChildQuery
argument_list|(
literal|"type"
argument_list|,
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|NumShards
name|twitter
init|=
name|getNumShards
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|BAD_REQUEST
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|getSuccessfulShards
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
name|response
operator|.
name|getIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|twitter
operator|.
name|numPrimaries
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getIndices
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getIndices
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|.
name|getFailedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|twitter
operator|.
name|numPrimaries
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getIndices
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|.
name|getFailures
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|twitter
operator|.
name|numPrimaries
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardOperationFailedException
name|failure
range|:
name|response
operator|.
name|getIndices
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|.
name|getFailures
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|failure
operator|.
name|reason
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"[test] [has_child] query and filter unsupported in delete_by_query api"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|failure
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|RestStatus
operator|.
name|BAD_REQUEST
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|failure
operator|.
name|shardId
argument_list|()
argument_list|,
name|greaterThan
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testDeleteByFieldQuery
specifier|public
name|void
name|testDeleteByFieldQuery
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addAlias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"alias"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|numDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|100
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
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setRouting
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|refresh
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchQuery
argument_list|(
literal|"_id"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|between
argument_list|(
literal|0
argument_list|,
name|numDocs
operator|-
literal|1
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|numDocs
argument_list|)
expr_stmt|;
name|DeleteByQueryResponse
name|deleteByQueryResponse
init|=
name|client
argument_list|()
operator|.
name|prepareDeleteByQuery
argument_list|(
name|indexOrAlias
argument_list|()
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchQuery
argument_list|(
literal|"_id"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|between
argument_list|(
literal|0
argument_list|,
name|numDocs
operator|-
literal|1
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|deleteByQueryResponse
operator|.
name|getIndices
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|deleteByQueryResponse
operator|.
name|getIndex
argument_list|(
literal|"test"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|numDocs
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testDateMath
specifier|public
name|void
name|testDateMath
parameter_list|()
throws|throws
name|Exception
block|{
name|index
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|,
literal|"d"
argument_list|,
literal|"2013-01-01"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareDeleteByQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|rangeQuery
argument_list|(
literal|"d"
argument_list|)
operator|.
name|to
argument_list|(
literal|"now-1h"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareCount
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testDeleteByTermQuery
specifier|public
name|void
name|testDeleteByTermQuery
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|int
name|numDocs
init|=
name|iterations
argument_list|(
literal|10
argument_list|,
literal|50
argument_list|)
decl_stmt|;
name|IndexRequestBuilder
index|[]
name|indexRequestBuilders
init|=
operator|new
name|IndexRequestBuilder
index|[
name|numDocs
operator|+
literal|1
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
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|indexRequestBuilders
index|[
name|i
index|]
operator|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
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
literal|"field"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
block|}
name|indexRequestBuilders
index|[
name|numDocs
index|]
operator|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|numDocs
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"other_value"
argument_list|)
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|indexRequestBuilders
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
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNoFailures
argument_list|(
name|searchResponse
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numDocs
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|DeleteByQueryResponse
name|deleteByQueryResponse
init|=
name|client
argument_list|()
operator|.
name|prepareDeleteByQuery
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|deleteByQueryResponse
operator|.
name|getIndices
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|IndexDeleteByQueryResponse
name|indexDeleteByQueryResponse
range|:
name|deleteByQueryResponse
control|)
block|{
name|assertThat
argument_list|(
name|indexDeleteByQueryResponse
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexDeleteByQueryResponse
operator|.
name|getFailures
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|refresh
argument_list|()
expr_stmt|;
name|searchResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNoFailures
argument_list|(
name|searchResponse
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|indexOrAlias
specifier|private
specifier|static
name|String
name|indexOrAlias
parameter_list|()
block|{
return|return
name|randomBoolean
argument_list|()
condition|?
literal|"test"
else|:
literal|"alias"
return|;
block|}
block|}
end_class

end_unit

