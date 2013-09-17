begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements. See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations under  * the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.percolator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|percolator
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
name|percolate
operator|.
name|MultiPercolateRequestBuilder
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
name|percolate
operator|.
name|MultiPercolateResponse
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
name|Requests
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
name|action
operator|.
name|percolate
operator|.
name|PercolateSourceBuilder
operator|.
name|docBuilder
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
name|percolator
operator|.
name|PercolatorTests
operator|.
name|convertFromTextArray
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|MultiPercolatorTests
specifier|public
class|class
name|MultiPercolatorTests
extends|extends
name|AbstractIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testBasics
specifier|public
name|void
name|testBasics
parameter_list|()
throws|throws
name|Exception
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> register a queries"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"_percolator"
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
literal|"query"
argument_list|,
name|matchQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"b"
argument_list|)
argument_list|)
operator|.
name|field
argument_list|(
literal|"a"
argument_list|,
literal|"b"
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
literal|"_percolator"
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
literal|"query"
argument_list|,
name|matchQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"c"
argument_list|)
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
literal|"_percolator"
argument_list|,
literal|"3"
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
literal|"query"
argument_list|,
name|boolQuery
argument_list|()
operator|.
name|must
argument_list|(
name|matchQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"b"
argument_list|)
argument_list|)
operator|.
name|must
argument_list|(
name|matchQuery
argument_list|(
literal|"field1"
argument_list|,
literal|"c"
argument_list|)
argument_list|)
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
literal|"_percolator"
argument_list|,
literal|"4"
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
literal|"query"
argument_list|,
name|matchAllQuery
argument_list|()
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
name|MultiPercolateResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareMultiPercolate
argument_list|()
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setPercolateDoc
argument_list|(
name|docBuilder
argument_list|()
operator|.
name|setDoc
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"b"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setPercolateDoc
argument_list|(
name|docBuilder
argument_list|()
operator|.
name|setDoc
argument_list|(
name|yamlBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"c"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setPercolateDoc
argument_list|(
name|docBuilder
argument_list|()
operator|.
name|setDoc
argument_list|(
name|smileBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"b c"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setPercolateDoc
argument_list|(
name|docBuilder
argument_list|()
operator|.
name|setDoc
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"d"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
comment|// non existing doc, so error element
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setGetRequest
argument_list|(
name|Requests
operator|.
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type"
argument_list|)
operator|.
name|id
argument_list|(
literal|"5"
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
name|MultiPercolateResponse
operator|.
name|Item
name|item
init|=
name|response
operator|.
name|getItems
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getMatches
argument_list|()
argument_list|,
name|arrayWithSize
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|errorMessage
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|convertFromTextArray
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getMatches
argument_list|()
argument_list|,
literal|"test"
argument_list|)
argument_list|,
name|arrayContainingInAnyOrder
argument_list|(
literal|"1"
argument_list|,
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|item
operator|=
name|response
operator|.
name|getItems
argument_list|()
index|[
literal|1
index|]
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|errorMessage
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoFailures
argument_list|(
name|item
operator|.
name|response
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getMatches
argument_list|()
argument_list|,
name|arrayWithSize
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getMatches
argument_list|()
argument_list|,
name|arrayWithSize
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|convertFromTextArray
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getMatches
argument_list|()
argument_list|,
literal|"test"
argument_list|)
argument_list|,
name|arrayContainingInAnyOrder
argument_list|(
literal|"2"
argument_list|,
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|item
operator|=
name|response
operator|.
name|getItems
argument_list|()
index|[
literal|2
index|]
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|errorMessage
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoFailures
argument_list|(
name|item
operator|.
name|response
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getMatches
argument_list|()
argument_list|,
name|arrayWithSize
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|convertFromTextArray
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getMatches
argument_list|()
argument_list|,
literal|"test"
argument_list|)
argument_list|,
name|arrayContainingInAnyOrder
argument_list|(
literal|"1"
argument_list|,
literal|"2"
argument_list|,
literal|"3"
argument_list|,
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|item
operator|=
name|response
operator|.
name|getItems
argument_list|()
index|[
literal|3
index|]
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|errorMessage
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertNoFailures
argument_list|(
name|item
operator|.
name|response
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getMatches
argument_list|()
argument_list|,
name|arrayWithSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|convertFromTextArray
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getMatches
argument_list|()
argument_list|,
literal|"test"
argument_list|)
argument_list|,
name|arrayContaining
argument_list|(
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|item
operator|=
name|response
operator|.
name|getItems
argument_list|()
index|[
literal|4
index|]
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|errorMessage
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|errorMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"document missing"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testExistingDocsOnly
specifier|public
name|void
name|testExistingDocsOnly
parameter_list|()
throws|throws
name|Exception
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
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|1
argument_list|)
operator|.
name|build
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
name|int
name|numQueries
init|=
name|randomIntBetween
argument_list|(
literal|50
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> register a queries"
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
name|numQueries
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
literal|"_percolator"
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
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"query"
argument_list|,
name|matchAllQuery
argument_list|()
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
block|}
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
literal|"field"
argument_list|,
literal|"a"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|MultiPercolateRequestBuilder
name|builder
init|=
name|client
argument_list|()
operator|.
name|prepareMultiPercolate
argument_list|()
decl_stmt|;
name|int
name|numPercolateRequest
init|=
name|randomIntBetween
argument_list|(
literal|50
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
name|numPercolateRequest
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setGetRequest
argument_list|(
name|Requests
operator|.
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|MultiPercolateResponse
name|response
init|=
name|builder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|items
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|numPercolateRequest
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|MultiPercolateResponse
operator|.
name|Item
name|item
range|:
name|response
control|)
block|{
name|assertThat
argument_list|(
name|item
operator|.
name|isFailure
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertNoFailures
argument_list|(
name|item
operator|.
name|response
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numQueries
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getMatches
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|numQueries
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Non existing doc
name|builder
operator|=
name|client
argument_list|()
operator|.
name|prepareMultiPercolate
argument_list|()
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
name|numPercolateRequest
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setGetRequest
argument_list|(
name|Requests
operator|.
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type"
argument_list|)
operator|.
name|id
argument_list|(
literal|"2"
argument_list|)
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|response
operator|=
name|builder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|items
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|numPercolateRequest
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|MultiPercolateResponse
operator|.
name|Item
name|item
range|:
name|response
control|)
block|{
name|assertThat
argument_list|(
name|item
operator|.
name|isFailure
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|errorMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"document missing"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// One existing doc
name|builder
operator|=
name|client
argument_list|()
operator|.
name|prepareMultiPercolate
argument_list|()
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
name|numPercolateRequest
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setGetRequest
argument_list|(
name|Requests
operator|.
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type"
argument_list|)
operator|.
name|id
argument_list|(
literal|"2"
argument_list|)
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setGetRequest
argument_list|(
name|Requests
operator|.
name|getRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
argument_list|)
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
argument_list|)
expr_stmt|;
name|response
operator|=
name|builder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|items
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|numPercolateRequest
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|items
argument_list|()
index|[
name|numPercolateRequest
index|]
operator|.
name|isFailure
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertNoFailures
argument_list|(
name|response
operator|.
name|items
argument_list|()
index|[
name|numPercolateRequest
index|]
operator|.
name|response
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|items
argument_list|()
index|[
name|numPercolateRequest
index|]
operator|.
name|getResponse
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numQueries
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|items
argument_list|()
index|[
name|numPercolateRequest
index|]
operator|.
name|getResponse
argument_list|()
operator|.
name|getMatches
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|numQueries
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testWithDocsOnly
specifier|public
name|void
name|testWithDocsOnly
parameter_list|()
throws|throws
name|Exception
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
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|2
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|1
argument_list|)
operator|.
name|build
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
name|int
name|numQueries
init|=
name|randomIntBetween
argument_list|(
literal|50
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> register a queries"
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
name|numQueries
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
literal|"_percolator"
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
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"query"
argument_list|,
name|matchAllQuery
argument_list|()
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
block|}
name|MultiPercolateRequestBuilder
name|builder
init|=
name|client
argument_list|()
operator|.
name|prepareMultiPercolate
argument_list|()
decl_stmt|;
name|int
name|numPercolateRequest
init|=
name|randomIntBetween
argument_list|(
literal|50
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
name|numPercolateRequest
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setPercolateDoc
argument_list|(
name|docBuilder
argument_list|()
operator|.
name|setDoc
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"a"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|MultiPercolateResponse
name|response
init|=
name|builder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|items
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|numPercolateRequest
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|MultiPercolateResponse
operator|.
name|Item
name|item
range|:
name|response
control|)
block|{
name|assertThat
argument_list|(
name|item
operator|.
name|isFailure
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertNoFailures
argument_list|(
name|item
operator|.
name|response
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numQueries
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getMatches
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|numQueries
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// All illegal json
name|builder
operator|=
name|client
argument_list|()
operator|.
name|prepareMultiPercolate
argument_list|()
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
name|numPercolateRequest
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"illegal json"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|response
operator|=
name|builder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|items
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|numPercolateRequest
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|MultiPercolateResponse
operator|.
name|Item
name|item
range|:
name|response
control|)
block|{
name|assertThat
argument_list|(
name|item
operator|.
name|isFailure
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|item
operator|.
name|getResponse
argument_list|()
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
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getShardFailures
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|ShardOperationFailedException
name|shardFailure
range|:
name|item
operator|.
name|getResponse
argument_list|()
operator|.
name|getShardFailures
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|shardFailure
operator|.
name|reason
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Failed to derive xcontent from"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|shardFailure
operator|.
name|status
argument_list|()
operator|.
name|getStatus
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|500
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// one valid request
name|builder
operator|=
name|client
argument_list|()
operator|.
name|prepareMultiPercolate
argument_list|()
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
name|numPercolateRequest
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"illegal json"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|preparePercolate
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setDocumentType
argument_list|(
literal|"type"
argument_list|)
operator|.
name|setPercolateDoc
argument_list|(
name|docBuilder
argument_list|()
operator|.
name|setDoc
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"a"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|response
operator|=
name|builder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|items
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|numPercolateRequest
operator|+
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|items
argument_list|()
index|[
name|numPercolateRequest
index|]
operator|.
name|isFailure
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertNoFailures
argument_list|(
name|response
operator|.
name|items
argument_list|()
index|[
name|numPercolateRequest
index|]
operator|.
name|getResponse
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|items
argument_list|()
index|[
name|numPercolateRequest
index|]
operator|.
name|getResponse
argument_list|()
operator|.
name|getCount
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numQueries
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|items
argument_list|()
index|[
name|numPercolateRequest
index|]
operator|.
name|getResponse
argument_list|()
operator|.
name|getMatches
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|numQueries
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

