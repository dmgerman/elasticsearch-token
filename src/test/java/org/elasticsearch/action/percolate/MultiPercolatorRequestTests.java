begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.percolate
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|percolate
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
name|collect
operator|.
name|MapBuilder
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
name|ElasticsearchTestCase
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
name|*
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|MultiPercolatorRequestTests
specifier|public
class|class
name|MultiPercolatorRequestTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testParseBulkRequests
specifier|public
name|void
name|testParseBulkRequests
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
literal|"/org/elasticsearch/action/percolate/mpercolate1.json"
argument_list|)
decl_stmt|;
name|MultiPercolateRequest
name|request
init|=
operator|new
name|MultiPercolateRequest
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
name|PercolateRequest
name|percolateRequest
init|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"my-index1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|documentType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-routing-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
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
name|percolateRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|onlyCount
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
name|percolateRequest
operator|.
name|getRequest
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Map
name|sourceMap
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|map
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|sourceMap
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|(
name|Object
operator|)
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|map
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|percolateRequest
operator|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"my-index2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indices
argument_list|()
index|[
literal|1
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"my-index3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|documentType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-routing-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
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
name|percolateRequest
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
name|percolateRequest
operator|.
name|onlyCount
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
name|percolateRequest
operator|.
name|getRequest
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|sourceMap
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|map
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|sourceMap
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|(
name|Object
operator|)
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"field1"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|map
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|percolateRequest
operator|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"my-index4"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indices
argument_list|()
index|[
literal|1
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"my-index5"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|documentType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-routing-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
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
name|percolateRequest
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
name|percolateRequest
operator|.
name|onlyCount
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
name|percolateRequest
operator|.
name|getRequest
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|sourceMap
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|map
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|sourceMap
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|(
name|Object
operator|)
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"field1"
argument_list|,
literal|"value3"
argument_list|)
operator|.
name|map
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|percolateRequest
operator|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"my-index6"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|documentType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-routing-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
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
name|percolateRequest
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
name|percolateRequest
operator|.
name|onlyCount
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
name|percolateRequest
operator|.
name|getRequest
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
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
name|percolateRequest
operator|.
name|getRequest
argument_list|()
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
operator|.
name|index
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-index6"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-routing-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
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
name|percolateRequest
operator|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"my-index7"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|documentType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-routing-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
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
name|percolateRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|onlyCount
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
name|percolateRequest
operator|.
name|getRequest
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
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
name|percolateRequest
operator|.
name|getRequest
argument_list|()
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
operator|.
name|index
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-index7"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-routing-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
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
name|percolateRequest
operator|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"my-index8"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|documentType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-routing-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|preference
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"primary"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|onlyCount
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
name|percolateRequest
operator|.
name|getRequest
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|sourceMap
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|map
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|sourceMap
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|(
name|Object
operator|)
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"field1"
argument_list|,
literal|"value4"
argument_list|)
operator|.
name|map
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|percolateRequest
operator|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|6
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"percolate-index1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|documentType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"other-type"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"percolate-routing-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
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
name|percolateRequest
operator|.
name|getRequest
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"my-index9"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
operator|.
name|routing
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
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
name|percolateRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|onlyCount
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
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|sourceMap
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|map
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|sourceMap
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|percolateRequest
operator|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|7
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"my-index10"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|documentType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|routing
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
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
name|percolateRequest
operator|.
name|getRequest
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"my-index10"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
operator|.
name|type
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
operator|.
name|routing
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|getRequest
argument_list|()
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
name|percolateRequest
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
literal|false
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
name|percolateRequest
operator|.
name|onlyCount
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
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|sourceMap
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|map
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|sourceMap
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testParseBulkRequests_defaults
specifier|public
name|void
name|testParseBulkRequests_defaults
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
literal|"/org/elasticsearch/action/percolate/mpercolate2.json"
argument_list|)
decl_stmt|;
name|MultiPercolateRequest
name|request
init|=
operator|new
name|MultiPercolateRequest
argument_list|()
decl_stmt|;
name|request
operator|.
name|indices
argument_list|(
literal|"my-index1"
argument_list|)
operator|.
name|documentType
argument_list|(
literal|"my-type1"
argument_list|)
operator|.
name|indicesOptions
argument_list|(
name|IndicesOptions
operator|.
name|lenientExpandOpen
argument_list|()
argument_list|)
expr_stmt|;
name|request
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
argument_list|)
expr_stmt|;
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
name|PercolateRequest
name|percolateRequest
init|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"my-index1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|documentType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-routing-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
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
name|percolateRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|IndicesOptions
operator|.
name|lenientExpandOpen
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|onlyCount
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
name|percolateRequest
operator|.
name|getRequest
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|Map
name|sourceMap
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|map
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|sourceMap
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|(
name|Object
operator|)
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|map
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|percolateRequest
operator|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"my-index1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|documentType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|routing
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-routing-1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
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
name|percolateRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|IndicesOptions
operator|.
name|lenientExpandOpen
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|onlyCount
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
name|percolateRequest
operator|.
name|getRequest
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|sourceMap
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|map
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|sourceMap
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|(
name|Object
operator|)
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"field1"
argument_list|,
literal|"value2"
argument_list|)
operator|.
name|map
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|percolateRequest
operator|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indices
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"my-index1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|documentType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"my-type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|IndicesOptions
operator|.
name|lenientExpandOpen
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|onlyCount
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
name|percolateRequest
operator|.
name|getRequest
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|sourceMap
operator|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|createParser
argument_list|(
name|percolateRequest
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|map
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|sourceMap
operator|.
name|get
argument_list|(
literal|"doc"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|(
name|Object
operator|)
name|MapBuilder
operator|.
name|newMapBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"field1"
argument_list|,
literal|"value3"
argument_list|)
operator|.
name|map
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

