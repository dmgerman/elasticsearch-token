begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|search
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
name|common
operator|.
name|io
operator|.
name|Streams
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
name|ToXContent
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
name|ESTestCase
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
name|nullValue
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|MultiSearchRequestTests
specifier|public
class|class
name|MultiSearchRequestTests
extends|extends
name|ESTestCase
block|{
annotation|@
name|Test
DECL|method|simpleAdd
specifier|public
name|void
name|simpleAdd
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|data
init|=
name|Streams
operator|.
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/action/search/simple-msearch1.json"
argument_list|)
decl_stmt|;
name|MultiSearchRequest
name|request
init|=
operator|new
name|MultiSearchRequest
argument_list|()
operator|.
name|add
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|types
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
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|equalTo
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
literal|true
argument_list|,
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|types
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|equalTo
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
argument_list|,
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|,
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|4
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|4
argument_list|)
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|5
argument_list|)
operator|.
name|indices
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|5
argument_list|)
operator|.
name|types
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
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|6
argument_list|)
operator|.
name|indices
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|6
argument_list|)
operator|.
name|types
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
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|6
argument_list|)
operator|.
name|searchType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|SearchType
operator|.
name|DFS_QUERY_THEN_FETCH
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|7
argument_list|)
operator|.
name|indices
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|7
argument_list|)
operator|.
name|types
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
annotation|@
name|Test
DECL|method|simpleAdd2
specifier|public
name|void
name|simpleAdd2
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|data
init|=
name|Streams
operator|.
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/action/search/simple-msearch2.json"
argument_list|)
decl_stmt|;
name|MultiSearchRequest
name|request
init|=
operator|new
name|MultiSearchRequest
argument_list|()
operator|.
name|add
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|types
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
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|types
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|indices
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|types
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
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|indices
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|types
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
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|searchType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|SearchType
operator|.
name|DFS_QUERY_THEN_FETCH
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|4
argument_list|)
operator|.
name|indices
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|4
argument_list|)
operator|.
name|types
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
annotation|@
name|Test
DECL|method|simpleAdd3
specifier|public
name|void
name|simpleAdd3
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|data
init|=
name|Streams
operator|.
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/action/search/simple-msearch3.json"
argument_list|)
decl_stmt|;
name|MultiSearchRequest
name|request
init|=
operator|new
name|MultiSearchRequest
argument_list|()
operator|.
name|add
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|1
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|1
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|types
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|1
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|types
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"type2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|types
argument_list|()
index|[
literal|1
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|indices
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|types
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
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|3
argument_list|)
operator|.
name|searchType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|SearchType
operator|.
name|DFS_QUERY_THEN_FETCH
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|simpleAdd4
specifier|public
name|void
name|simpleAdd4
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|data
init|=
name|Streams
operator|.
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/action/search/simple-msearch4.json"
argument_list|)
decl_stmt|;
name|MultiSearchRequest
name|request
init|=
operator|new
name|MultiSearchRequest
argument_list|()
operator|.
name|add
argument_list|(
name|data
argument_list|,
literal|0
argument_list|,
name|data
operator|.
name|length
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test0"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|1
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|requestCache
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
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|preference
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|1
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|types
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|requestCache
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|preference
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"_local"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|indices
argument_list|()
index|[
literal|1
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"test1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|types
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"type2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|types
argument_list|()
index|[
literal|1
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"123"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testResponseErrorToXContent
specifier|public
name|void
name|testResponseErrorToXContent
parameter_list|()
throws|throws
name|IOException
block|{
name|MultiSearchResponse
name|response
init|=
operator|new
name|MultiSearchResponse
argument_list|(
operator|new
name|MultiSearchResponse
operator|.
name|Item
index|[]
block|{
operator|new
name|MultiSearchResponse
operator|.
name|Item
argument_list|(
literal|null
argument_list|,
operator|new
name|IllegalStateException
argument_list|(
literal|"foobar"
argument_list|)
argument_list|)
block|,
operator|new
name|MultiSearchResponse
operator|.
name|Item
argument_list|(
literal|null
argument_list|,
operator|new
name|IllegalStateException
argument_list|(
literal|"baaaaaazzzz"
argument_list|)
argument_list|)
block|}
argument_list|)
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|response
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|ToXContent
operator|.
name|EMPTY_PARAMS
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"\"responses\"[{\"error\":{\"root_cause\":[{\"type\":\"illegal_state_exception\",\"reason\":\"foobar\"}],\"type\":\"illegal_state_exception\",\"reason\":\"foobar\"}},{\"error\":{\"root_cause\":[{\"type\":\"illegal_state_exception\",\"reason\":\"baaaaaazzzz\"}],\"type\":\"illegal_state_exception\",\"reason\":\"baaaaaazzzz\"}}]"
argument_list|,
name|builder
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

