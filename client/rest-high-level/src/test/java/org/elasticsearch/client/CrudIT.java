begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|entity
operator|.
name|ContentType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|entity
operator|.
name|StringEntity
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|DocWriteResponse
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
name|delete
operator|.
name|DeleteRequest
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
name|delete
operator|.
name|DeleteResponse
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
name|GetRequest
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
name|GetResponse
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
name|Strings
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
name|search
operator|.
name|fetch
operator|.
name|subphase
operator|.
name|FetchSourceContext
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
name|Collections
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
name|CoreMatchers
operator|.
name|containsString
import|;
end_import

begin_class
DECL|class|CrudIT
specifier|public
class|class
name|CrudIT
extends|extends
name|ESRestHighLevelClientTestCase
block|{
DECL|method|testExists
specifier|public
name|void
name|testExists
parameter_list|()
throws|throws
name|IOException
block|{
block|{
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|execute
argument_list|(
name|getRequest
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|exists
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|existsAsync
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|document
init|=
literal|"{\"field1\":\"value1\",\"field2\":\"value2\"}"
decl_stmt|;
name|StringEntity
name|stringEntity
init|=
operator|new
name|StringEntity
argument_list|(
name|document
argument_list|,
name|ContentType
operator|.
name|APPLICATION_JSON
argument_list|)
decl_stmt|;
name|Response
name|response
init|=
name|client
argument_list|()
operator|.
name|performRequest
argument_list|(
literal|"PUT"
argument_list|,
literal|"/index/type/id"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"refresh"
argument_list|,
literal|"wait_for"
argument_list|)
argument_list|,
name|stringEntity
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|201
argument_list|,
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|)
expr_stmt|;
block|{
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|execute
argument_list|(
name|getRequest
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|exists
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|existsAsync
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|{
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"does_not_exist"
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|execute
argument_list|(
name|getRequest
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|exists
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|existsAsync
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|{
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"does_not_exist"
argument_list|)
operator|.
name|version
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|ElasticsearchException
name|exception
init|=
name|expectThrows
argument_list|(
name|ElasticsearchException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|execute
argument_list|(
name|getRequest
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|exists
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|existsAsync
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|RestStatus
operator|.
name|BAD_REQUEST
argument_list|,
name|exception
operator|.
name|status
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"/index/type/does_not_exist?version=1: HTTP/1.1 400 Bad Request"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testGet
specifier|public
name|void
name|testGet
parameter_list|()
throws|throws
name|IOException
block|{
block|{
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|)
decl_stmt|;
name|ElasticsearchException
name|exception
init|=
name|expectThrows
argument_list|(
name|ElasticsearchException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|execute
argument_list|(
name|getRequest
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|get
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|getAsync
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|RestStatus
operator|.
name|NOT_FOUND
argument_list|,
name|exception
operator|.
name|status
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Elasticsearch exception [type=index_not_found_exception, reason=no such index]"
argument_list|,
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"index"
argument_list|,
name|exception
operator|.
name|getMetadata
argument_list|(
literal|"es.index"
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|document
init|=
literal|"{\"field1\":\"value1\",\"field2\":\"value2\"}"
decl_stmt|;
name|StringEntity
name|stringEntity
init|=
operator|new
name|StringEntity
argument_list|(
name|document
argument_list|,
name|ContentType
operator|.
name|APPLICATION_JSON
argument_list|)
decl_stmt|;
name|Response
name|response
init|=
name|client
argument_list|()
operator|.
name|performRequest
argument_list|(
literal|"PUT"
argument_list|,
literal|"/index/type/id"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"refresh"
argument_list|,
literal|"wait_for"
argument_list|)
argument_list|,
name|stringEntity
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|201
argument_list|,
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|)
expr_stmt|;
block|{
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|)
operator|.
name|version
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|ElasticsearchException
name|exception
init|=
name|expectThrows
argument_list|(
name|ElasticsearchException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|execute
argument_list|(
name|getRequest
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|get
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|getAsync
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|RestStatus
operator|.
name|CONFLICT
argument_list|,
name|exception
operator|.
name|status
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Elasticsearch exception [type=version_conflict_engine_exception, "
operator|+
literal|"reason=[type][id]: "
operator|+
literal|"version conflict, current version [1] is different than the one provided [2]]"
argument_list|,
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"index"
argument_list|,
name|exception
operator|.
name|getMetadata
argument_list|(
literal|"es.index"
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|{
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|getRequest
operator|.
name|version
argument_list|(
literal|1L
argument_list|)
expr_stmt|;
block|}
name|GetResponse
name|getResponse
init|=
name|execute
argument_list|(
name|getRequest
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|get
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|getAsync
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"index"
argument_list|,
name|getResponse
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"type"
argument_list|,
name|getResponse
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|getResponse
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|getResponse
operator|.
name|isSourceEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|getResponse
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|document
argument_list|,
name|getResponse
operator|.
name|getSourceAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|{
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"does_not_exist"
argument_list|)
decl_stmt|;
name|GetResponse
name|getResponse
init|=
name|execute
argument_list|(
name|getRequest
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|get
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|getAsync
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"index"
argument_list|,
name|getResponse
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"type"
argument_list|,
name|getResponse
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"does_not_exist"
argument_list|,
name|getResponse
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|-
literal|1
argument_list|,
name|getResponse
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|getResponse
operator|.
name|isSourceEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|getResponse
operator|.
name|getSourceAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|{
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|)
decl_stmt|;
name|getRequest
operator|.
name|fetchSourceContext
argument_list|(
operator|new
name|FetchSourceContext
argument_list|(
literal|false
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
name|GetResponse
name|getResponse
init|=
name|execute
argument_list|(
name|getRequest
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|get
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|getAsync
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"index"
argument_list|,
name|getResponse
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"type"
argument_list|,
name|getResponse
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|getResponse
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|getResponse
operator|.
name|isSourceEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|getResponse
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|getResponse
operator|.
name|getSourceAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|{
name|GetRequest
name|getRequest
init|=
operator|new
name|GetRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|getRequest
operator|.
name|fetchSourceContext
argument_list|(
operator|new
name|FetchSourceContext
argument_list|(
literal|true
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"field1"
block|}
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|getRequest
operator|.
name|fetchSourceContext
argument_list|(
operator|new
name|FetchSourceContext
argument_list|(
literal|true
argument_list|,
name|Strings
operator|.
name|EMPTY_ARRAY
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"field2"
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|GetResponse
name|getResponse
init|=
name|execute
argument_list|(
name|getRequest
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|get
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|getAsync
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"index"
argument_list|,
name|getResponse
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"type"
argument_list|,
name|getResponse
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|getResponse
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|getResponse
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|getResponse
operator|.
name|isSourceEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1L
argument_list|,
name|getResponse
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|sourceAsMap
init|=
name|getResponse
operator|.
name|getSourceAsMap
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|sourceAsMap
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"value1"
argument_list|,
name|sourceAsMap
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testDelete
specifier|public
name|void
name|testDelete
parameter_list|()
throws|throws
name|IOException
block|{
block|{
name|DeleteRequest
name|deleteRequest
init|=
operator|new
name|DeleteRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"does_not_exist"
argument_list|)
decl_stmt|;
name|DeleteResponse
name|deleteResponse
init|=
name|execute
argument_list|(
name|deleteRequest
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|delete
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|deleteAsync
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"index"
argument_list|,
name|deleteResponse
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"type"
argument_list|,
name|deleteResponse
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"does_not_exist"
argument_list|,
name|deleteResponse
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DocWriteResponse
operator|.
name|Result
operator|.
name|NOT_FOUND
argument_list|,
name|deleteResponse
operator|.
name|getResult
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|deleteResponse
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|String
name|document
init|=
literal|"{\"field1\":\"value1\",\"field2\":\"value2\"}"
decl_stmt|;
name|StringEntity
name|stringEntity
init|=
operator|new
name|StringEntity
argument_list|(
name|document
argument_list|,
name|ContentType
operator|.
name|APPLICATION_JSON
argument_list|)
decl_stmt|;
name|Response
name|response
init|=
name|client
argument_list|()
operator|.
name|performRequest
argument_list|(
literal|"PUT"
argument_list|,
literal|"/index/type/id"
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"refresh"
argument_list|,
literal|"wait_for"
argument_list|)
argument_list|,
name|stringEntity
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|201
argument_list|,
name|response
operator|.
name|getStatusLine
argument_list|()
operator|.
name|getStatusCode
argument_list|()
argument_list|)
expr_stmt|;
block|{
name|DeleteRequest
name|deleteRequest
init|=
operator|new
name|DeleteRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|)
operator|.
name|version
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|ElasticsearchException
name|exception
init|=
name|expectThrows
argument_list|(
name|ElasticsearchException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|execute
argument_list|(
name|deleteRequest
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|delete
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|deleteAsync
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|RestStatus
operator|.
name|CONFLICT
argument_list|,
name|exception
operator|.
name|status
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Elasticsearch exception [type=version_conflict_engine_exception, "
operator|+
literal|"reason=[type][id]: "
operator|+
literal|"version conflict, current version [1] is different than the one provided [2]]"
argument_list|,
name|exception
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"index"
argument_list|,
name|exception
operator|.
name|getMetadata
argument_list|(
literal|"es.index"
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|{
name|DeleteRequest
name|deleteRequest
init|=
operator|new
name|DeleteRequest
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|deleteRequest
operator|.
name|version
argument_list|(
literal|1L
argument_list|)
expr_stmt|;
block|}
name|DeleteResponse
name|deleteResponse
init|=
name|execute
argument_list|(
name|deleteRequest
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|delete
argument_list|,
name|highLevelClient
argument_list|()
operator|::
name|deleteAsync
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"index"
argument_list|,
name|deleteResponse
operator|.
name|getIndex
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"type"
argument_list|,
name|deleteResponse
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"id"
argument_list|,
name|deleteResponse
operator|.
name|getId
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|DocWriteResponse
operator|.
name|Result
operator|.
name|DELETED
argument_list|,
name|deleteResponse
operator|.
name|getResult
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

