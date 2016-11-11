begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.document
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|document
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
name|indices
operator|.
name|cache
operator|.
name|clear
operator|.
name|ClearIndicesCacheResponse
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
name|flush
operator|.
name|FlushResponse
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
name|forcemerge
operator|.
name|ForceMergeResponse
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
name|refresh
operator|.
name|RefreshResponse
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
name|bulk
operator|.
name|BulkResponse
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
name|GetResponse
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
name|IndexResponse
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
name|WriteRequest
operator|.
name|RefreshPolicy
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
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
name|ESIntegTestCase
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
name|io
operator|.
name|IOException
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
name|DocWriteRequest
operator|.
name|OpType
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
name|clearIndicesCacheRequest
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
name|getRequest
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
name|indexRequest
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
name|refreshRequest
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
name|termQuery
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
comment|/**  * Integration test for document action like index, bulk, and get. It has a very long history: it was in the second commit of Elasticsearch.  */
end_comment

begin_class
DECL|class|DocumentActionsIT
specifier|public
class|class
name|DocumentActionsIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|createIndex
specifier|protected
name|void
name|createIndex
parameter_list|()
block|{
name|ElasticsearchAssertions
operator|.
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
literal|"name"
argument_list|,
literal|"type=keyword,store=true"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|getConcreteIndexName
specifier|protected
name|String
name|getConcreteIndexName
parameter_list|()
block|{
return|return
literal|"test"
return|;
block|}
DECL|method|testIndexActions
specifier|public
name|void
name|testIndexActions
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|()
expr_stmt|;
name|NumShards
name|numShards
init|=
name|getNumShards
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Running Cluster Health"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Indexing [type1/1]"
argument_list|)
expr_stmt|;
name|IndexResponse
name|indexResponse
init|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|()
operator|.
name|setIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setId
argument_list|(
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
operator|.
name|setRefreshPolicy
argument_list|(
name|RefreshPolicy
operator|.
name|IMMEDIATE
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|indexResponse
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexResponse
operator|.
name|getId
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
name|indexResponse
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Refreshing"
argument_list|)
expr_stmt|;
name|RefreshResponse
name|refreshResponse
init|=
name|refresh
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|refreshResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numShards
operator|.
name|totalNumShards
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> index exists?"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexExists
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> index exists?, fake index"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|indexExists
argument_list|(
literal|"test1234565"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Clearing cache"
argument_list|)
expr_stmt|;
name|ClearIndicesCacheResponse
name|clearIndicesCacheResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|clearCache
argument_list|(
name|clearIndicesCacheRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|recycler
argument_list|(
literal|true
argument_list|)
operator|.
name|fieldDataCache
argument_list|(
literal|true
argument_list|)
operator|.
name|queryCache
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertNoFailures
argument_list|(
name|clearIndicesCacheResponse
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clearIndicesCacheResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numShards
operator|.
name|totalNumShards
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Force Merging"
argument_list|)
expr_stmt|;
name|waitForRelocation
argument_list|(
name|ClusterHealthStatus
operator|.
name|GREEN
argument_list|)
expr_stmt|;
name|ForceMergeResponse
name|mergeResponse
init|=
name|forceMerge
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|mergeResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numShards
operator|.
name|totalNumShards
argument_list|)
argument_list|)
expr_stmt|;
name|GetResponse
name|getResult
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Get [type1/1]"
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setOperationThreaded
argument_list|(
literal|false
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
name|getResult
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"cycle #"
operator|+
name|i
argument_list|,
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|string
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"cycle(map) #"
operator|+
name|i
argument_list|,
operator|(
name|String
operator|)
name|getResult
operator|.
name|getSourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"name"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
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
name|operationThreaded
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"cycle #"
operator|+
name|i
argument_list|,
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|string
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"Get [type1/1] with script"
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|prepareGet
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setStoredFields
argument_list|(
literal|"name"
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
name|getResult
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|isExists
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
name|getResult
operator|.
name|getSourceAsBytes
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getField
argument_list|(
literal|"name"
argument_list|)
operator|.
name|getValues
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"Get [type1/2] (should be empty)"
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
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
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|isExists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"Delete [type1/1]"
argument_list|)
expr_stmt|;
name|DeleteResponse
name|deleteResponse
init|=
name|client
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
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
name|assertThat
argument_list|(
name|deleteResponse
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|deleteResponse
operator|.
name|getId
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
name|deleteResponse
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Refreshing"
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
name|refresh
argument_list|(
name|refreshRequest
argument_list|(
literal|"test"
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
literal|"Get [type1/1] (should be empty)"
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
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
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|isExists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"Index [type1/1]"
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
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
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
literal|"Index [type1/2]"
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
literal|"2"
argument_list|)
operator|.
name|source
argument_list|(
name|source
argument_list|(
literal|"2"
argument_list|,
literal|"test2"
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
literal|"Flushing"
argument_list|)
expr_stmt|;
name|FlushResponse
name|flushResult
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|(
literal|"test"
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
name|flushResult
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numShards
operator|.
name|totalNumShards
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|flushResult
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
name|logger
operator|.
name|info
argument_list|(
literal|"Refreshing"
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
name|refresh
argument_list|(
name|refreshRequest
argument_list|(
literal|"test"
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
literal|"Get [type1/1] and [type1/2]"
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
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
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"cycle #"
operator|+
name|i
argument_list|,
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|string
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
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
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|String
name|ste1
init|=
name|getResult
operator|.
name|getSourceAsString
argument_list|()
decl_stmt|;
name|String
name|ste2
init|=
name|source
argument_list|(
literal|"2"
argument_list|,
literal|"test2"
argument_list|)
operator|.
name|string
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"cycle #"
operator|+
name|i
argument_list|,
name|ste1
argument_list|,
name|equalTo
argument_list|(
name|ste2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"Count"
argument_list|)
expr_stmt|;
comment|// check count
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|5
condition|;
name|i
operator|++
control|)
block|{
comment|// test successful
name|SearchResponse
name|countResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"_type"
argument_list|,
literal|"type1"
argument_list|)
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
name|countResponse
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|countResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|countResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numShards
operator|.
name|numPrimaries
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|countResponse
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
comment|// count with no query is a match all one
name|countResponse
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
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
literal|"Failures "
operator|+
name|countResponse
operator|.
name|getShardFailures
argument_list|()
argument_list|,
name|countResponse
operator|.
name|getShardFailures
argument_list|()
operator|==
literal|null
condition|?
literal|0
else|:
name|countResponse
operator|.
name|getShardFailures
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
name|countResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|countResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numShards
operator|.
name|numPrimaries
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|countResponse
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
block|}
block|}
DECL|method|testBulk
specifier|public
name|void
name|testBulk
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|()
expr_stmt|;
name|NumShards
name|numShards
init|=
name|getNumShards
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"-> running Cluster Health"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|BulkResponse
name|bulkResponse
init|=
name|client
argument_list|()
operator|.
name|prepareBulk
argument_list|()
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|()
operator|.
name|setIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setId
argument_list|(
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|(
literal|"1"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|()
operator|.
name|setIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setId
argument_list|(
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|(
literal|"2"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
operator|.
name|setCreate
argument_list|(
literal|true
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|()
operator|.
name|setIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
name|source
argument_list|(
literal|"3"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareDelete
argument_list|()
operator|.
name|setIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setId
argument_list|(
literal|"1"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|()
operator|.
name|setIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{ xxx }"
argument_list|)
argument_list|)
comment|// failure
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|hasFailures
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
name|bulkResponse
operator|.
name|getItems
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|0
index|]
operator|.
name|isFailed
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
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|0
index|]
operator|.
name|getOpType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|OpType
operator|.
name|INDEX
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|0
index|]
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|0
index|]
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|0
index|]
operator|.
name|getId
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
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|1
index|]
operator|.
name|isFailed
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
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|1
index|]
operator|.
name|getOpType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|OpType
operator|.
name|CREATE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|1
index|]
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|1
index|]
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|1
index|]
operator|.
name|getId
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
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|2
index|]
operator|.
name|isFailed
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
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|2
index|]
operator|.
name|getOpType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|OpType
operator|.
name|INDEX
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|2
index|]
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|2
index|]
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|generatedId3
init|=
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|2
index|]
operator|.
name|getId
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|3
index|]
operator|.
name|isFailed
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
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|3
index|]
operator|.
name|getOpType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|OpType
operator|.
name|DELETE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|3
index|]
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|3
index|]
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|3
index|]
operator|.
name|getId
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
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|4
index|]
operator|.
name|isFailed
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
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|4
index|]
operator|.
name|getOpType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|OpType
operator|.
name|INDEX
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|4
index|]
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|4
index|]
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
expr_stmt|;
name|waitForRelocation
argument_list|(
name|ClusterHealthStatus
operator|.
name|GREEN
argument_list|)
expr_stmt|;
name|RefreshResponse
name|refreshResponse
init|=
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
literal|"test"
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
name|refreshResponse
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|refreshResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numShards
operator|.
name|totalNumShards
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
name|GetResponse
name|getResult
init|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
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
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"cycle #"
operator|+
name|i
argument_list|,
name|getResult
operator|.
name|isExists
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
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
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"cycle #"
operator|+
name|i
argument_list|,
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"2"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|string
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
name|client
argument_list|()
operator|.
name|get
argument_list|(
name|getRequest
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
name|generatedId3
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
literal|"cycle #"
operator|+
name|i
argument_list|,
name|getResult
operator|.
name|getSourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"3"
argument_list|,
literal|"test"
argument_list|)
operator|.
name|string
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|source
specifier|private
name|XContentBuilder
name|source
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|nameValue
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"id"
argument_list|,
name|id
argument_list|)
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
name|nameValue
argument_list|)
operator|.
name|endObject
argument_list|()
return|;
block|}
block|}
end_class

end_unit

