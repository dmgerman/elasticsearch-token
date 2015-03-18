begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.expression
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|expression
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|index
operator|.
name|query
operator|.
name|functionscore
operator|.
name|ScoreFunctionBuilder
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
name|functionscore
operator|.
name|ScoreFunctionBuilders
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
name|SearchHits
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
name|AggregationBuilders
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
name|search
operator|.
name|sort
operator|.
name|SortBuilders
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
name|sort
operator|.
name|SortOrder
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|greaterThan
import|;
end_import

begin_class
DECL|class|ExpressionScriptTests
specifier|public
class|class
name|ExpressionScriptTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|method|buildRequest
specifier|private
name|SearchRequestBuilder
name|buildRequest
parameter_list|(
name|String
name|script
parameter_list|,
name|Object
modifier|...
name|params
parameter_list|)
block|{
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|paramsMap
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
assert|assert
operator|(
name|params
operator|.
name|length
operator|%
literal|2
operator|==
literal|0
operator|)
assert|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|params
operator|.
name|length
condition|;
name|i
operator|+=
literal|2
control|)
block|{
name|paramsMap
operator|.
name|put
argument_list|(
name|params
index|[
name|i
index|]
operator|.
name|toString
argument_list|()
argument_list|,
name|params
index|[
name|i
operator|+
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
name|SearchRequestBuilder
name|req
init|=
operator|new
name|SearchRequestBuilder
argument_list|(
name|client
argument_list|()
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|req
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addSort
argument_list|(
name|SortBuilders
operator|.
name|fieldSort
argument_list|(
literal|"_uid"
argument_list|)
operator|.
name|order
argument_list|(
name|SortOrder
operator|.
name|ASC
argument_list|)
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"foo"
argument_list|,
literal|"expression"
argument_list|,
name|script
argument_list|,
name|paramsMap
argument_list|)
expr_stmt|;
return|return
name|req
return|;
block|}
DECL|method|testBasic
specifier|public
name|void
name|testBasic
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|4
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|SearchResponse
name|rsp
init|=
name|buildRequest
argument_list|(
literal|"doc['foo'].value + 1"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|rsp
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5.0
argument_list|,
name|rsp
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|field
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testScore
specifier|public
name|void
name|testScore
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"hello goodbye"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"hello hello hello goodbye"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"hello hello goodebye"
argument_list|)
argument_list|)
expr_stmt|;
name|ScoreFunctionBuilder
name|score
init|=
name|ScoreFunctionBuilders
operator|.
name|scriptFunction
argument_list|(
literal|"1 / _score"
argument_list|,
literal|"expression"
argument_list|)
decl_stmt|;
name|SearchRequestBuilder
name|req
init|=
operator|new
name|SearchRequestBuilder
argument_list|(
name|client
argument_list|()
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|req
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|functionScoreQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"text"
argument_list|,
literal|"hello"
argument_list|)
argument_list|,
name|score
argument_list|)
operator|.
name|boostMode
argument_list|(
literal|"replace"
argument_list|)
argument_list|)
expr_stmt|;
name|req
operator|.
name|setSearchType
argument_list|(
name|SearchType
operator|.
name|DFS_QUERY_THEN_FETCH
argument_list|)
expr_stmt|;
comment|// make sure DF is consistent
name|SearchResponse
name|rsp
init|=
name|req
operator|.
name|get
argument_list|()
decl_stmt|;
name|SearchHits
name|hits
init|=
name|rsp
operator|.
name|getHits
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|hits
operator|.
name|getTotalHits
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"1"
argument_list|,
name|hits
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"3"
argument_list|,
name|hits
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"2"
argument_list|,
name|hits
operator|.
name|getAt
argument_list|(
literal|2
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSparseField
specifier|public
name|void
name|testSparseField
parameter_list|()
throws|throws
name|Exception
block|{
name|ElasticsearchAssertions
operator|.
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"doc"
argument_list|,
literal|"x"
argument_list|,
literal|"type=long"
argument_list|,
literal|"y"
argument_list|,
literal|"type=long"
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"x"
argument_list|,
literal|4
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"y"
argument_list|,
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|rsp
init|=
name|buildRequest
argument_list|(
literal|"doc['x'].value + 1"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|ElasticsearchAssertions
operator|.
name|assertSearchResponse
argument_list|(
name|rsp
argument_list|)
expr_stmt|;
name|SearchHits
name|hits
init|=
name|rsp
operator|.
name|getHits
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|rsp
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|5.0
argument_list|,
name|hits
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|field
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1.0
argument_list|,
name|hits
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|field
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testMissingField
specifier|public
name|void
name|testMissingField
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"x"
argument_list|,
literal|4
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
block|{
name|buildRequest
argument_list|(
literal|"doc['bogus'].value"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Expected missing field to cause failure"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"should have contained ExpressionScriptCompilationException"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"ExpressionScriptCompilationException"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"should have contained missing field error"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"does not exist in mappings"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testParams
specifier|public
name|void
name|testParams
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"x"
argument_list|,
literal|10
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"x"
argument_list|,
literal|3
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"x"
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
comment|// a = int, b = double, c = long
name|String
name|script
init|=
literal|"doc['x'].value * a + b + ((c + doc['x'].value)> 5000000009 ? 1 : 0)"
decl_stmt|;
name|SearchResponse
name|rsp
init|=
name|buildRequest
argument_list|(
name|script
argument_list|,
literal|"a"
argument_list|,
literal|2
argument_list|,
literal|"b"
argument_list|,
literal|3.5
argument_list|,
literal|"c"
argument_list|,
literal|5000000000L
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|SearchHits
name|hits
init|=
name|rsp
operator|.
name|getHits
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|hits
operator|.
name|getTotalHits
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|24.5
argument_list|,
name|hits
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|field
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|9.5
argument_list|,
name|hits
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|field
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|13.5
argument_list|,
name|hits
operator|.
name|getAt
argument_list|(
literal|2
argument_list|)
operator|.
name|field
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testCompileFailure
specifier|public
name|void
name|testCompileFailure
parameter_list|()
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"x"
argument_list|,
literal|1
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
block|{
name|buildRequest
argument_list|(
literal|"garbage%@#%@"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Expected expression compilation failure"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"should have contained ExpressionScriptCompilationException"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"ExpressionScriptCompilationException"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"should have contained compilation failure"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"Failed to parse expression"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testNonNumericParam
specifier|public
name|void
name|testNonNumericParam
parameter_list|()
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"x"
argument_list|,
literal|1
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
block|{
name|buildRequest
argument_list|(
literal|"a"
argument_list|,
literal|"a"
argument_list|,
literal|"astring"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Expected string parameter to cause failure"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"should have contained ExpressionScriptCompilationException"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"ExpressionScriptCompilationException"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"should have contained non-numeric parameter error"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"must be a numeric type"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testNonNumericField
specifier|public
name|void
name|testNonNumericField
parameter_list|()
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"this is not a number"
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
block|{
name|buildRequest
argument_list|(
literal|"doc['text'].value"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Expected text field to cause execution failure"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"should have contained ExpressionScriptCompilationException"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"ExpressionScriptCompilationException"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"should have contained non-numeric field error"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"must be numeric"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testInvalidGlobalVariable
specifier|public
name|void
name|testInvalidGlobalVariable
parameter_list|()
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|5
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
block|{
name|buildRequest
argument_list|(
literal|"bogus"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Expected bogus variable to cause execution failure"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"should have contained ExpressionScriptCompilationException"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"ExpressionScriptCompilationException"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"should have contained unknown variable error"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"Unknown variable"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testDocWithoutField
specifier|public
name|void
name|testDocWithoutField
parameter_list|()
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|5
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
block|{
name|buildRequest
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Expected doc variable without field to cause execution failure"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"should have contained ExpressionScriptCompilationException"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"ExpressionScriptCompilationException"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"should have contained a missing specific field error"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"must be used with a specific field"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testInvalidFieldMember
specifier|public
name|void
name|testInvalidFieldMember
parameter_list|()
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"foo"
argument_list|,
literal|5
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
block|{
name|buildRequest
argument_list|(
literal|"doc['foo'].bogus"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Expected bogus field member to cause execution failure"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"should have contained ExpressionScriptCompilationException"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"ExpressionScriptCompilationException"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|+
literal|"should have contained field member error"
argument_list|,
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
operator|.
name|contains
argument_list|(
literal|"Invalid member for field"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSpecialValueVariable
specifier|public
name|void
name|testSpecialValueVariable
parameter_list|()
throws|throws
name|Exception
block|{
comment|// i.e. _value for aggregations
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"x"
argument_list|,
literal|5
argument_list|,
literal|"y"
argument_list|,
literal|1.2
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"x"
argument_list|,
literal|10
argument_list|,
literal|"y"
argument_list|,
literal|1.4
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"x"
argument_list|,
literal|13
argument_list|,
literal|"y"
argument_list|,
literal|1.8
argument_list|)
argument_list|)
expr_stmt|;
name|SearchRequestBuilder
name|req
init|=
operator|new
name|SearchRequestBuilder
argument_list|(
name|client
argument_list|()
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|req
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|AggregationBuilders
operator|.
name|stats
argument_list|(
literal|"int_agg"
argument_list|)
operator|.
name|field
argument_list|(
literal|"x"
argument_list|)
operator|.
name|script
argument_list|(
literal|"_value * 3"
argument_list|)
operator|.
name|lang
argument_list|(
name|ExpressionScriptEngineService
operator|.
name|NAME
argument_list|)
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|AggregationBuilders
operator|.
name|stats
argument_list|(
literal|"double_agg"
argument_list|)
operator|.
name|field
argument_list|(
literal|"y"
argument_list|)
operator|.
name|script
argument_list|(
literal|"_value - 1.1"
argument_list|)
operator|.
name|lang
argument_list|(
name|ExpressionScriptEngineService
operator|.
name|NAME
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|rsp
init|=
name|req
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|rsp
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|)
expr_stmt|;
name|Stats
name|stats
init|=
name|rsp
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"int_agg"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|39.0
argument_list|,
name|stats
operator|.
name|getMax
argument_list|()
argument_list|,
literal|0.0001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|15.0
argument_list|,
name|stats
operator|.
name|getMin
argument_list|()
argument_list|,
literal|0.0001
argument_list|)
expr_stmt|;
name|stats
operator|=
name|rsp
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"double_agg"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0.7
argument_list|,
name|stats
operator|.
name|getMax
argument_list|()
argument_list|,
literal|0.0001
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0.1
argument_list|,
name|stats
operator|.
name|getMin
argument_list|()
argument_list|,
literal|0.0001
argument_list|)
expr_stmt|;
block|}
DECL|method|testStringSpecialValueVariable
specifier|public
name|void
name|testStringSpecialValueVariable
parameter_list|()
throws|throws
name|Exception
block|{
comment|// i.e. expression script for term aggregations, which is not allowed
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"hello"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"goodbye"
argument_list|)
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"doc"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"text"
argument_list|,
literal|"hello"
argument_list|)
argument_list|)
expr_stmt|;
name|SearchRequestBuilder
name|req
init|=
operator|new
name|SearchRequestBuilder
argument_list|(
name|client
argument_list|()
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|req
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addAggregation
argument_list|(
name|AggregationBuilders
operator|.
name|terms
argument_list|(
literal|"term_agg"
argument_list|)
operator|.
name|field
argument_list|(
literal|"text"
argument_list|)
operator|.
name|script
argument_list|(
literal|"_value"
argument_list|)
operator|.
name|lang
argument_list|(
name|ExpressionScriptEngineService
operator|.
name|NAME
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|message
decl_stmt|;
try|try
block|{
comment|// shards that don't have docs with the "text" field will not fail,
comment|// so we may or may not get a total failure
name|SearchResponse
name|rsp
init|=
name|req
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|rsp
operator|.
name|getShardFailures
argument_list|()
operator|.
name|length
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// at least the shards containing the docs should have failed
name|message
operator|=
name|rsp
operator|.
name|getShardFailures
argument_list|()
index|[
literal|0
index|]
operator|.
name|reason
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|message
operator|=
name|ExceptionsHelper
operator|.
name|detailedMessage
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|message
operator|+
literal|"should have contained ExpressionScriptExecutionException"
argument_list|,
name|message
operator|.
name|contains
argument_list|(
literal|"ExpressionScriptExecutionException"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|message
operator|+
literal|"should have contained text variable error"
argument_list|,
name|message
operator|.
name|contains
argument_list|(
literal|"text variable"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

