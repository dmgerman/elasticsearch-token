begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.search.customscore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|search
operator|.
name|customscore
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
name|action
operator|.
name|search
operator|.
name|SearchType
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
name|test
operator|.
name|integration
operator|.
name|AbstractNodesTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|AfterMethod
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|BeforeMethod
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
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
name|*
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
name|xcontent
operator|.
name|QueryBuilders
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
name|search
operator|.
name|builder
operator|.
name|SearchSourceBuilder
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
name|MatcherAssert
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
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
annotation|@
name|Test
DECL|class|CustomScoreSearchTests
specifier|public
class|class
name|CustomScoreSearchTests
extends|extends
name|AbstractNodesTests
block|{
DECL|field|client
specifier|private
name|Client
name|client
decl_stmt|;
DECL|method|createNodes
annotation|@
name|BeforeMethod
specifier|public
name|void
name|createNodes
parameter_list|()
throws|throws
name|Exception
block|{
name|startNode
argument_list|(
literal|"server1"
argument_list|)
expr_stmt|;
name|client
operator|=
name|getClient
argument_list|()
expr_stmt|;
block|}
DECL|method|closeNodes
annotation|@
name|AfterMethod
specifier|public
name|void
name|closeNodes
parameter_list|()
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
name|closeAllNodes
argument_list|()
expr_stmt|;
block|}
DECL|method|getClient
specifier|protected
name|Client
name|getClient
parameter_list|()
block|{
return|return
name|client
argument_list|(
literal|"server1"
argument_list|)
return|;
block|}
DECL|method|testCustomScriptBoost
annotation|@
name|Test
specifier|public
name|void
name|testCustomScriptBoost
parameter_list|()
throws|throws
name|Exception
block|{
comment|// execute a search before we create an index
try|try
block|{
name|client
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
assert|assert
literal|false
operator|:
literal|"should fail"
assert|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore, no indices
block|}
try|try
block|{
name|client
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
assert|assert
literal|false
operator|:
literal|"should fail"
assert|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore, no indices
block|}
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|create
argument_list|(
name|createIndexRequest
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
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
literal|"test"
argument_list|,
literal|"value beck"
argument_list|)
operator|.
name|field
argument_list|(
literal|"num1"
argument_list|,
literal|1.0f
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
literal|"test"
argument_list|,
literal|"value check"
argument_list|)
operator|.
name|field
argument_list|(
literal|"num1"
argument_list|,
literal|2.0f
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
literal|"--- QUERY_THEN_FETCH"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"running doc['num1'].value"
argument_list|)
expr_stmt|;
name|SearchResponse
name|response
init|=
name|client
operator|.
name|search
argument_list|(
name|searchRequest
argument_list|()
operator|.
name|searchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|source
argument_list|(
name|searchSource
argument_list|()
operator|.
name|explain
argument_list|(
literal|true
argument_list|)
operator|.
name|query
argument_list|(
name|customScoreQuery
argument_list|(
name|termQuery
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|script
argument_list|(
literal|"doc['num1'].value"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|hits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Hit[0] {} Explanation {}"
argument_list|,
name|response
operator|.
name|hits
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
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|explanation
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Hit[1] {} Explanation {}"
argument_list|,
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|explanation
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|hits
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
name|assertThat
argument_list|(
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"running -doc['num1'].value"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|search
argument_list|(
name|searchRequest
argument_list|()
operator|.
name|searchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|source
argument_list|(
name|searchSource
argument_list|()
operator|.
name|explain
argument_list|(
literal|true
argument_list|)
operator|.
name|query
argument_list|(
name|customScoreQuery
argument_list|(
name|termQuery
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|script
argument_list|(
literal|"-doc['num1'].value"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|hits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Hit[0] {} Explanation {}"
argument_list|,
name|response
operator|.
name|hits
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
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|explanation
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Hit[1] {} Explanation {}"
argument_list|,
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|explanation
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|hits
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
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
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
name|logger
operator|.
name|info
argument_list|(
literal|"running pow(doc['num1'].value, 2)"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|search
argument_list|(
name|searchRequest
argument_list|()
operator|.
name|searchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|source
argument_list|(
name|searchSource
argument_list|()
operator|.
name|explain
argument_list|(
literal|true
argument_list|)
operator|.
name|query
argument_list|(
name|customScoreQuery
argument_list|(
name|termQuery
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|script
argument_list|(
literal|"pow(doc['num1'].value, 2)"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|hits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Hit[0] {} Explanation {}"
argument_list|,
name|response
operator|.
name|hits
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
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|explanation
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Hit[1] {} Explanation {}"
argument_list|,
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|explanation
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|hits
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
name|assertThat
argument_list|(
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"running max(doc['num1'].value, 1)"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|search
argument_list|(
name|searchRequest
argument_list|()
operator|.
name|searchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|source
argument_list|(
name|searchSource
argument_list|()
operator|.
name|explain
argument_list|(
literal|true
argument_list|)
operator|.
name|query
argument_list|(
name|customScoreQuery
argument_list|(
name|termQuery
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|script
argument_list|(
literal|"max(doc['num1'].value, 1d)"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|hits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Hit[0] {} Explanation {}"
argument_list|,
name|response
operator|.
name|hits
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
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|explanation
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Hit[1] {} Explanation {}"
argument_list|,
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|explanation
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|hits
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
name|assertThat
argument_list|(
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"running doc['num1'].value * _score"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|search
argument_list|(
name|searchRequest
argument_list|()
operator|.
name|searchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|source
argument_list|(
name|searchSource
argument_list|()
operator|.
name|explain
argument_list|(
literal|true
argument_list|)
operator|.
name|query
argument_list|(
name|customScoreQuery
argument_list|(
name|termQuery
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|script
argument_list|(
literal|"doc['num1'].value * _score"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|hits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Hit[0] {} Explanation {}"
argument_list|,
name|response
operator|.
name|hits
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
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|explanation
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Hit[1] {} Explanation {}"
argument_list|,
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|explanation
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|hits
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
name|assertThat
argument_list|(
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"running param1 * param2 * _score"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
operator|.
name|search
argument_list|(
name|searchRequest
argument_list|()
operator|.
name|searchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|source
argument_list|(
name|searchSource
argument_list|()
operator|.
name|explain
argument_list|(
literal|true
argument_list|)
operator|.
name|query
argument_list|(
name|customScoreQuery
argument_list|(
name|termQuery
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|script
argument_list|(
literal|"param1 * param2 * _score"
argument_list|)
operator|.
name|param
argument_list|(
literal|"param1"
argument_list|,
literal|2
argument_list|)
operator|.
name|param
argument_list|(
literal|"param2"
argument_list|,
literal|2
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|hits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Hit[0] {} Explanation {}"
argument_list|,
name|response
operator|.
name|hits
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
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|explanation
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Hit[1] {} Explanation {}"
argument_list|,
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|explanation
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|hits
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
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|hits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
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
block|}
end_class

end_unit

