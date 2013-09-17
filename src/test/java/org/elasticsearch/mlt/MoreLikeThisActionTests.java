begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.mlt
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|mlt
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
name|admin
operator|.
name|cluster
operator|.
name|health
operator|.
name|ClusterHealthStatus
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
name|client
operator|.
name|Client
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
name|xcontent
operator|.
name|XContentFactory
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
name|AbstractIntegrationTest
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
name|client
operator|.
name|Requests
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
name|FilterBuilders
operator|.
name|termFilter
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
name|moreLikeThisFieldQuery
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
name|moreLikeThisQuery
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
name|ElasticSearchAssertions
operator|.
name|*
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
name|notNullValue
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|MoreLikeThisActionTests
specifier|public
class|class
name|MoreLikeThisActionTests
extends|extends
name|AbstractIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testSimpleMoreLikeThis
specifier|public
name|void
name|testSimpleMoreLikeThis
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Creating index test"
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"text"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
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
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Running Cluster Health"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ensureGreen
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|GREEN
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Indexing..."
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|source
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"lucene"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"2"
argument_list|)
operator|.
name|source
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"lucene release"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
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
name|refresh
argument_list|(
name|refreshRequest
argument_list|()
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Running moreLikeThis"
argument_list|)
expr_stmt|;
name|SearchResponse
name|mltResponse
init|=
name|client
argument_list|()
operator|.
name|moreLikeThis
argument_list|(
name|moreLikeThisRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|minTermFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|minDocFreq
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|mltResponse
argument_list|,
literal|1l
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testSimpleMoreLikeOnLongField
specifier|public
name|void
name|testSimpleMoreLikeOnLongField
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Creating index test"
argument_list|)
expr_stmt|;
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Running Cluster Health"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ensureGreen
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|GREEN
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Indexing..."
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|source
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"some_long"
argument_list|,
literal|1367484649580l
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type2"
argument_list|)
operator|.
name|id
argument_list|(
literal|"2"
argument_list|)
operator|.
name|source
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"some_long"
argument_list|,
literal|0
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"3"
argument_list|)
operator|.
name|source
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"some_long"
argument_list|,
operator|-
literal|666
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
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
name|refresh
argument_list|(
name|refreshRequest
argument_list|()
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Running moreLikeThis"
argument_list|)
expr_stmt|;
name|SearchResponse
name|mltResponse
init|=
name|client
argument_list|()
operator|.
name|moreLikeThis
argument_list|(
name|moreLikeThisRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|minTermFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|minDocFreq
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|mltResponse
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMoreLikeThisWithAliases
specifier|public
name|void
name|testMoreLikeThisWithAliases
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Creating index test"
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"text"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
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
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Creating aliases alias release"
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
name|aliases
argument_list|(
name|indexAliasesRequest
argument_list|()
operator|.
name|addAlias
argument_list|(
literal|"test"
argument_list|,
literal|"release"
argument_list|,
name|termFilter
argument_list|(
literal|"text"
argument_list|,
literal|"release"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
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
name|aliases
argument_list|(
name|indexAliasesRequest
argument_list|()
operator|.
name|addAlias
argument_list|(
literal|"test"
argument_list|,
literal|"beta"
argument_list|,
name|termFilter
argument_list|(
literal|"text"
argument_list|,
literal|"beta"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Running Cluster Health"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ensureGreen
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|GREEN
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Indexing..."
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|source
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"lucene beta"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"2"
argument_list|)
operator|.
name|source
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"lucene release"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"3"
argument_list|)
operator|.
name|source
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"elasticsearch beta"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"4"
argument_list|)
operator|.
name|source
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"text"
argument_list|,
literal|"elasticsearch release"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
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
name|refresh
argument_list|(
name|refreshRequest
argument_list|()
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Running moreLikeThis on index"
argument_list|)
expr_stmt|;
name|SearchResponse
name|mltResponse
init|=
name|client
argument_list|()
operator|.
name|moreLikeThis
argument_list|(
name|moreLikeThisRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|minTermFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|minDocFreq
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|mltResponse
argument_list|,
literal|2l
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Running moreLikeThis on beta shard"
argument_list|)
expr_stmt|;
name|mltResponse
operator|=
name|client
argument_list|()
operator|.
name|moreLikeThis
argument_list|(
name|moreLikeThisRequest
argument_list|(
literal|"beta"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|minTermFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|minDocFreq
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|mltResponse
argument_list|,
literal|1l
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mltResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Running moreLikeThis on release shard"
argument_list|)
expr_stmt|;
name|mltResponse
operator|=
name|client
argument_list|()
operator|.
name|moreLikeThis
argument_list|(
name|moreLikeThisRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|minTermFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|minDocFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|searchIndices
argument_list|(
literal|"release"
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|mltResponse
argument_list|,
literal|1l
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mltResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMoreLikeThisIssue2197
specifier|public
name|void
name|testMoreLikeThisIssue2197
parameter_list|()
throws|throws
name|Exception
block|{
name|Client
name|client
init|=
name|client
argument_list|()
decl_stmt|;
name|String
name|mapping
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"bar"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
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
operator|.
name|string
argument_list|()
decl_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"bar"
argument_list|,
name|mapping
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
literal|"foo"
argument_list|,
literal|"bar"
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
name|startObject
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"bar"
argument_list|,
literal|"boz"
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
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|(
literal|"foo"
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
name|ensureGreen
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ClusterHealthStatus
operator|.
name|GREEN
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareMoreLikeThis
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
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
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
name|client
operator|.
name|prepareMoreLikeThis
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
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
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
comment|// See: https://github.com/elasticsearch/elasticsearch/issues/2489
DECL|method|testMoreLikeWithCustomRouting
specifier|public
name|void
name|testMoreLikeWithCustomRouting
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"bar"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
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
operator|.
name|string
argument_list|()
decl_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"bar"
argument_list|,
name|mapping
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
literal|"foo"
argument_list|,
literal|"bar"
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
name|startObject
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"bar"
argument_list|,
literal|"boz"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|setRouting
argument_list|(
literal|"2"
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
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareMoreLikeThis
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setRouting
argument_list|(
literal|"2"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
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
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
comment|// See issue: https://github.com/elasticsearch/elasticsearch/issues/3039
DECL|method|testMoreLikeThisIssueRoutingNotSerialized
specifier|public
name|void
name|testMoreLikeThisIssueRoutingNotSerialized
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"bar"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
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
operator|.
name|string
argument_list|()
decl_stmt|;
name|prepareCreate
argument_list|(
literal|"foo"
argument_list|,
literal|2
argument_list|,
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|2
argument_list|)
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"bar"
argument_list|,
name|mapping
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
literal|"foo"
argument_list|,
literal|"bar"
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
name|startObject
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|field
argument_list|(
literal|"bar"
argument_list|,
literal|"boz"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
operator|.
name|setRouting
argument_list|(
literal|"4000"
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
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareMoreLikeThis
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setRouting
argument_list|(
literal|"4000"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
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
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
literal|"int_value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|randomNumericType
argument_list|(
name|getRandom
argument_list|()
argument_list|)
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"string_value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
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
comment|// Implicit list of fields -> ignore numeric fields
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareMoreLikeThis
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setMinDocFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|setMinTermFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|1l
argument_list|)
expr_stmt|;
comment|// Explicit list of fields including numeric fields -> fail
name|assertThrows
argument_list|(
name|client
argument_list|()
operator|.
name|prepareMoreLikeThis
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setField
argument_list|(
literal|"string_value"
argument_list|,
literal|"int_value"
argument_list|)
argument_list|,
name|SearchPhaseExecutionException
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// mlt query with no field -> OK
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
name|moreLikeThisQuery
argument_list|()
operator|.
name|likeText
argument_list|(
literal|"index"
argument_list|)
operator|.
name|minTermFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|minDocFreq
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|2l
argument_list|)
expr_stmt|;
comment|// mlt query with string fields
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
name|moreLikeThisQuery
argument_list|(
literal|"string_value"
argument_list|)
operator|.
name|likeText
argument_list|(
literal|"index"
argument_list|)
operator|.
name|minTermFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|minDocFreq
argument_list|(
literal|1
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|2l
argument_list|)
expr_stmt|;
comment|// mlt query with at least a numeric field -> fail by default
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
name|moreLikeThisQuery
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
comment|// mlt query with at least a numeric field -> fail by command
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
name|moreLikeThisQuery
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
comment|// mlt query with at least a numeric field but fail_on_unsupported_field set to false
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
name|moreLikeThisQuery
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
name|minTermFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|minDocFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|failOnUnsupportedField
argument_list|(
literal|false
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|2l
argument_list|)
expr_stmt|;
comment|// mlt field query on a numeric field -> failure by default
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
name|moreLikeThisFieldQuery
argument_list|(
literal|"int_value"
argument_list|)
operator|.
name|likeText
argument_list|(
literal|"42"
argument_list|)
operator|.
name|minTermFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|minDocFreq
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|,
name|SearchPhaseExecutionException
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// mlt field query on a numeric field -> failure by command
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
name|moreLikeThisFieldQuery
argument_list|(
literal|"int_value"
argument_list|)
operator|.
name|likeText
argument_list|(
literal|"42"
argument_list|)
operator|.
name|minTermFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|minDocFreq
argument_list|(
literal|1
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
comment|// mlt field query on a numeric field but fail_on_unsupported_field set to false
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
name|moreLikeThisFieldQuery
argument_list|(
literal|"int_value"
argument_list|)
operator|.
name|likeText
argument_list|(
literal|"42"
argument_list|)
operator|.
name|minTermFreq
argument_list|(
literal|1
argument_list|)
operator|.
name|minDocFreq
argument_list|(
literal|1
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
name|assertHitCount
argument_list|(
name|searchResponse
argument_list|,
literal|0l
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

