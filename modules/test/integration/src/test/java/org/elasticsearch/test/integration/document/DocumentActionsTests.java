begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.document
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
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
name|cluster
operator|.
name|health
operator|.
name|ClusterHealthResponse
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
name|optimize
operator|.
name|OptimizeResponse
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
name|count
operator|.
name|CountResponse
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
name|support
operator|.
name|broadcast
operator|.
name|BroadcastOperationThreading
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
name|replication
operator|.
name|ReplicationType
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
name|Unicode
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
DECL|class|DocumentActionsTests
specifier|public
class|class
name|DocumentActionsTests
extends|extends
name|AbstractNodesTests
block|{
DECL|field|client1
specifier|protected
name|Client
name|client1
decl_stmt|;
DECL|field|client2
specifier|protected
name|Client
name|client2
decl_stmt|;
DECL|method|startNodes
annotation|@
name|BeforeMethod
specifier|public
name|void
name|startNodes
parameter_list|()
block|{
name|startNode
argument_list|(
literal|"server1"
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"server2"
argument_list|)
expr_stmt|;
name|client1
operator|=
name|getClient1
argument_list|()
expr_stmt|;
name|client2
operator|=
name|getClient2
argument_list|()
expr_stmt|;
comment|// no indices, check that simple operations fail
try|try
block|{
name|client1
operator|.
name|prepareCount
argument_list|(
literal|"test"
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
name|setOperationThreading
argument_list|(
name|BroadcastOperationThreading
operator|.
name|NO_THREADS
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
comment|// all is well
block|}
name|client1
operator|.
name|prepareCount
argument_list|()
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
name|setOperationThreading
argument_list|(
name|BroadcastOperationThreading
operator|.
name|NO_THREADS
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|createIndex
argument_list|()
expr_stmt|;
block|}
DECL|method|createIndex
specifier|protected
name|void
name|createIndex
parameter_list|()
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Creating index test"
argument_list|)
expr_stmt|;
name|client1
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
DECL|method|closeNodes
annotation|@
name|AfterMethod
specifier|public
name|void
name|closeNodes
parameter_list|()
block|{
name|client1
operator|.
name|close
argument_list|()
expr_stmt|;
name|client2
operator|.
name|close
argument_list|()
expr_stmt|;
name|closeAllNodes
argument_list|()
expr_stmt|;
block|}
DECL|method|getClient1
specifier|protected
name|Client
name|getClient1
parameter_list|()
block|{
return|return
name|client
argument_list|(
literal|"server1"
argument_list|)
return|;
block|}
DECL|method|getClient2
specifier|protected
name|Client
name|getClient2
parameter_list|()
block|{
return|return
name|client
argument_list|(
literal|"server2"
argument_list|)
return|;
block|}
DECL|method|testIndexActions
annotation|@
name|Test
specifier|public
name|void
name|testIndexActions
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"Running Cluster Health"
argument_list|)
expr_stmt|;
name|ClusterHealthResponse
name|clusterHealth
init|=
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|health
argument_list|(
name|clusterHealth
argument_list|()
operator|.
name|waitForGreenStatus
argument_list|()
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Done Cluster Health, status "
operator|+
name|clusterHealth
operator|.
name|status
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clusterHealth
operator|.
name|timedOut
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
name|clusterHealth
operator|.
name|status
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
literal|"Indexing [type1/1]"
argument_list|)
expr_stmt|;
name|IndexResponse
name|indexResponse
init|=
name|client1
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
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|indexResponse
operator|.
name|index
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
name|indexResponse
operator|.
name|type
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
name|client1
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
name|assertThat
argument_list|(
name|refreshResponse
operator|.
name|successfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|refreshResponse
operator|.
name|failedShards
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
literal|"Clearing cache"
argument_list|)
expr_stmt|;
name|ClearIndicesCacheResponse
name|clearIndicesCacheResponse
init|=
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|clearCache
argument_list|(
name|clearIndicesCache
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|clearIndicesCacheResponse
operator|.
name|successfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|clearIndicesCacheResponse
operator|.
name|failedShards
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
literal|"Optimizing"
argument_list|)
expr_stmt|;
name|OptimizeResponse
name|optimizeResponse
init|=
name|client1
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareOptimize
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
name|optimizeResponse
operator|.
name|successfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|optimizeResponse
operator|.
name|failedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
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
name|client1
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
name|index
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
name|sourceAsString
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
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"cycle(map) #"
operator|+
name|i
argument_list|,
call|(
name|String
call|)
argument_list|(
operator|(
name|Map
operator|)
name|getResult
operator|.
name|sourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"type1"
argument_list|)
argument_list|)
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
name|client1
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
name|sourceAsString
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
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|index
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
name|client1
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
name|exists
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
name|client1
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
name|setReplicationType
argument_list|(
name|ReplicationType
operator|.
name|SYNC
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
name|index
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
name|deleteResponse
operator|.
name|type
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
name|client1
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
name|client1
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
name|exists
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
name|client1
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
name|client1
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
name|client1
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
name|successfulShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|flushResult
operator|.
name|failedShards
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
name|client1
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
name|client1
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
name|index
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
name|sourceAsString
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
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
name|client1
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
name|sourceAsString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|source
argument_list|(
literal|"2"
argument_list|,
literal|"test2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
operator|.
name|index
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
name|CountResponse
name|countResponse
init|=
name|client1
operator|.
name|prepareCount
argument_list|(
literal|"test"
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
name|setOperationThreading
argument_list|(
name|BroadcastOperationThreading
operator|.
name|NO_THREADS
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
name|countResponse
operator|.
name|count
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
name|countResponse
operator|.
name|successfulShards
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
name|countResponse
operator|.
name|failedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|countResponse
operator|=
name|client1
operator|.
name|count
argument_list|(
name|countRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|query
argument_list|(
name|termQuery
argument_list|(
literal|"_type"
argument_list|,
literal|"type1"
argument_list|)
argument_list|)
operator|.
name|operationThreading
argument_list|(
name|BroadcastOperationThreading
operator|.
name|SINGLE_THREAD
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|countResponse
operator|.
name|count
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
name|countResponse
operator|.
name|successfulShards
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
name|countResponse
operator|.
name|failedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|countResponse
operator|=
name|client1
operator|.
name|count
argument_list|(
name|countRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|query
argument_list|(
name|termQuery
argument_list|(
literal|"_type"
argument_list|,
literal|"type1"
argument_list|)
argument_list|)
operator|.
name|operationThreading
argument_list|(
name|BroadcastOperationThreading
operator|.
name|THREAD_PER_SHARD
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|countResponse
operator|.
name|count
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
name|countResponse
operator|.
name|successfulShards
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
name|countResponse
operator|.
name|failedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// test failed (simply query that can't be parsed)
name|countResponse
operator|=
name|client1
operator|.
name|count
argument_list|(
name|countRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|query
argument_list|(
name|Unicode
operator|.
name|fromStringAsBytes
argument_list|(
literal|"{ term : { _type : \"type1 } }"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|countResponse
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|countResponse
operator|.
name|successfulShards
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
name|countResponse
operator|.
name|failedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"Delete by query"
argument_list|)
expr_stmt|;
name|DeleteByQueryResponse
name|queryResponse
init|=
name|client2
operator|.
name|prepareDeleteByQuery
argument_list|()
operator|.
name|setIndices
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"name"
argument_list|,
literal|"test2"
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
name|queryResponse
operator|.
name|index
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
operator|.
name|successfulShards
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
name|queryResponse
operator|.
name|index
argument_list|(
name|getConcreteIndexName
argument_list|()
argument_list|)
operator|.
name|failedShards
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|client1
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
literal|"Get [type1/1] and [type1/2], should be empty"
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
name|client1
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
name|index
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
name|sourceAsString
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
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
name|client1
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
name|exists
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
name|getResult
operator|.
name|index
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
name|String
name|source
parameter_list|(
name|String
name|id
parameter_list|,
name|String
name|nameValue
parameter_list|)
block|{
return|return
literal|"{ type1 : { \"id\" : \""
operator|+
name|id
operator|+
literal|"\", \"name\" : \""
operator|+
name|nameValue
operator|+
literal|"\" } }"
return|;
block|}
block|}
end_class

end_unit

