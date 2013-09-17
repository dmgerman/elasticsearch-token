begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.mget
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|mget
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
name|get
operator|.
name|MultiGetItemResponse
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
name|get
operator|.
name|MultiGetRequest
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
name|get
operator|.
name|MultiGetRequestBuilder
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
name|get
operator|.
name|MultiGetResponse
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
name|bytes
operator|.
name|BytesReference
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
name|fetch
operator|.
name|source
operator|.
name|FetchSourceContext
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
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|*
import|;
end_import

begin_class
DECL|class|SimpleMgetTests
specifier|public
class|class
name|SimpleMgetTests
extends|extends
name|AbstractIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testThatMgetShouldWorkWithOneIndexMissing
specifier|public
name|void
name|testThatMgetShouldWorkWithOneIndexMissing
parameter_list|()
throws|throws
name|IOException
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
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
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|endObject
argument_list|()
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
name|MultiGetResponse
name|mgetResponse
init|=
name|client
argument_list|()
operator|.
name|prepareMultiGet
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|MultiGetRequest
operator|.
name|Item
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|MultiGetRequest
operator|.
name|Item
argument_list|(
literal|"nonExistingIndex"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
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
name|mgetResponse
operator|.
name|getResponses
argument_list|()
operator|.
name|length
argument_list|,
name|is
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|0
index|]
operator|.
name|getIndex
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|0
index|]
operator|.
name|isFailed
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|1
index|]
operator|.
name|getIndex
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"nonExistingIndex"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|1
index|]
operator|.
name|isFailed
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|1
index|]
operator|.
name|getFailure
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"[nonExistingIndex] missing"
argument_list|)
argument_list|)
expr_stmt|;
name|mgetResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareMultiGet
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|MultiGetRequest
operator|.
name|Item
argument_list|(
literal|"nonExistingIndex"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
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
name|mgetResponse
operator|.
name|getResponses
argument_list|()
operator|.
name|length
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|0
index|]
operator|.
name|getIndex
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"nonExistingIndex"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|0
index|]
operator|.
name|isFailed
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|0
index|]
operator|.
name|getFailure
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"[nonExistingIndex] missing"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testThatParentPerDocumentIsSupported
specifier|public
name|void
name|testThatParentPerDocumentIsSupported
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureYellow
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
name|preparePutMapping
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"test"
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
literal|"test"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_parent"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"foo"
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
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setParent
argument_list|(
literal|"4"
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
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
literal|"foo"
argument_list|,
literal|"bar"
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
name|MultiGetResponse
name|mgetResponse
init|=
name|client
argument_list|()
operator|.
name|prepareMultiGet
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|MultiGetRequest
operator|.
name|Item
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|parent
argument_list|(
literal|"4"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|MultiGetRequest
operator|.
name|Item
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
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
name|mgetResponse
operator|.
name|getResponses
argument_list|()
operator|.
name|length
argument_list|,
name|is
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|0
index|]
operator|.
name|isFailed
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|0
index|]
operator|.
name|getResponse
argument_list|()
operator|.
name|isExists
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|1
index|]
operator|.
name|isFailed
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|1
index|]
operator|.
name|getResponse
argument_list|()
operator|.
name|isExists
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Test
DECL|method|testThatSourceFilteringIsSupported
specifier|public
name|void
name|testThatSourceFilteringIsSupported
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
name|BytesReference
name|sourceBytesRef
init|=
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
literal|"1"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"included"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"should be seen"
argument_list|)
operator|.
name|field
argument_list|(
literal|"hidden_field"
argument_list|,
literal|"should not be seen"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"excluded"
argument_list|,
literal|"should not be seen"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
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
literal|100
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
name|sourceBytesRef
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|MultiGetRequestBuilder
name|request
init|=
name|client
argument_list|()
operator|.
name|prepareMultiGet
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|%
literal|2
operator|==
literal|0
condition|)
block|{
name|request
operator|.
name|add
argument_list|(
operator|new
name|MultiGetRequest
operator|.
name|Item
argument_list|(
literal|"test"
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
name|fetchSourceContext
argument_list|(
operator|new
name|FetchSourceContext
argument_list|(
literal|"included"
argument_list|,
literal|"*.hidden_field"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|request
operator|.
name|add
argument_list|(
operator|new
name|MultiGetRequest
operator|.
name|Item
argument_list|(
literal|"test"
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
name|fetchSourceContext
argument_list|(
operator|new
name|FetchSourceContext
argument_list|(
literal|false
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|MultiGetResponse
name|response
init|=
name|request
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getResponses
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|100
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|MultiGetItemResponse
name|responseItem
init|=
name|response
operator|.
name|getResponses
argument_list|()
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|i
operator|%
literal|2
operator|==
literal|0
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
init|=
name|responseItem
operator|.
name|getResponse
argument_list|()
operator|.
name|getSourceAsMap
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|source
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
name|source
argument_list|,
name|hasKey
argument_list|(
literal|"included"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|source
operator|.
name|get
argument_list|(
literal|"included"
argument_list|)
operator|)
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
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|source
operator|.
name|get
argument_list|(
literal|"included"
argument_list|)
operator|)
argument_list|,
name|hasKey
argument_list|(
literal|"field"
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|responseItem
operator|.
name|getResponse
argument_list|()
operator|.
name|getSourceAsBytes
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
DECL|method|testThatRoutingPerDocumentIsSupported
specifier|public
name|void
name|testThatRoutingPerDocumentIsSupported
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureYellow
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|setRouting
argument_list|(
literal|"bar"
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
literal|"foo"
argument_list|,
literal|"bar"
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
name|MultiGetResponse
name|mgetResponse
init|=
name|client
argument_list|()
operator|.
name|prepareMultiGet
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|MultiGetRequest
operator|.
name|Item
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|routing
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|MultiGetRequest
operator|.
name|Item
argument_list|(
literal|"test"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
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
name|mgetResponse
operator|.
name|getResponses
argument_list|()
operator|.
name|length
argument_list|,
name|is
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|0
index|]
operator|.
name|isFailed
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|0
index|]
operator|.
name|getResponse
argument_list|()
operator|.
name|isExists
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|1
index|]
operator|.
name|isFailed
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mgetResponse
operator|.
name|getResponses
argument_list|()
index|[
literal|1
index|]
operator|.
name|getResponse
argument_list|()
operator|.
name|isExists
argument_list|()
argument_list|,
name|is
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

