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
name|Version
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
name|ActionListener
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
name|ActionFilter
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
name|ActionFilters
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
name|TransportAction
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
name|ClusterName
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
name|ClusterState
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
name|metadata
operator|.
name|IndexNameExpressionResolver
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
name|node
operator|.
name|DiscoveryNode
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
name|node
operator|.
name|DiscoveryNodes
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
name|service
operator|.
name|ClusterService
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
name|Settings
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
name|transport
operator|.
name|LocalTransportAddress
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
operator|.
name|TaskManager
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
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicReference
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

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
import|;
end_import

begin_class
DECL|class|TransportMultiSearchActionTests
specifier|public
class|class
name|TransportMultiSearchActionTests
extends|extends
name|ESTestCase
block|{
DECL|method|testBatchExecute
specifier|public
name|void
name|testBatchExecute
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Initialize depedencies of TransportMultiSearchAction
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"node.name"
argument_list|,
name|TransportMultiSearchActionTests
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ActionFilters
name|actionFilters
init|=
name|mock
argument_list|(
name|ActionFilters
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|actionFilters
operator|.
name|filters
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|ActionFilter
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|(
name|settings
argument_list|)
decl_stmt|;
name|TaskManager
name|taskManager
init|=
name|mock
argument_list|(
name|TaskManager
operator|.
name|class
argument_list|)
decl_stmt|;
name|TransportService
name|transportService
init|=
name|mock
argument_list|(
name|TransportService
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|transportService
operator|.
name|getTaskManager
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|taskManager
argument_list|)
expr_stmt|;
name|ClusterService
name|clusterService
init|=
name|mock
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"test"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|IndexNameExpressionResolver
name|resolver
init|=
operator|new
name|IndexNameExpressionResolver
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
comment|// Keep track of the number of concurrent searches started by multi search api,
comment|// and if there are more searches than is allowed create an error and remember that.
name|int
name|maxAllowedConcurrentSearches
init|=
name|scaledRandomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
argument_list|)
decl_stmt|;
name|AtomicInteger
name|counter
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
name|AtomicReference
argument_list|<
name|AssertionError
argument_list|>
name|errorHolder
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|TransportAction
argument_list|<
name|SearchRequest
argument_list|,
name|SearchResponse
argument_list|>
name|searchAction
init|=
operator|new
name|TransportAction
argument_list|<
name|SearchRequest
argument_list|,
name|SearchResponse
argument_list|>
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
literal|"action"
argument_list|,
name|threadPool
argument_list|,
name|actionFilters
argument_list|,
name|resolver
argument_list|,
name|taskManager
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|doExecute
parameter_list|(
name|SearchRequest
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|SearchResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|int
name|currentConcurrentSearches
init|=
name|counter
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentConcurrentSearches
operator|>
name|maxAllowedConcurrentSearches
condition|)
block|{
name|errorHolder
operator|.
name|set
argument_list|(
operator|new
name|AssertionError
argument_list|(
literal|"Current concurrent search ["
operator|+
name|currentConcurrentSearches
operator|+
literal|"] is higher than is allowed ["
operator|+
name|maxAllowedConcurrentSearches
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|threadPool
operator|.
name|executor
argument_list|(
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
argument_list|)
operator|.
name|execute
argument_list|(
parameter_list|()
lambda|->
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|scaledRandomIntBetween
argument_list|(
literal|10
argument_list|,
literal|1000
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{                             }
name|counter
operator|.
name|decrementAndGet
argument_list|()
expr_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|SearchResponse
argument_list|()
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|TransportMultiSearchAction
name|action
init|=
operator|new
name|TransportMultiSearchAction
argument_list|(
name|threadPool
argument_list|,
name|actionFilters
argument_list|,
name|transportService
argument_list|,
name|clusterService
argument_list|,
name|searchAction
argument_list|,
name|resolver
argument_list|,
literal|10
argument_list|)
decl_stmt|;
comment|// Execute the multi search api and fail if we find an error after executing:
try|try
block|{
name|int
name|numSearchRequests
init|=
name|randomIntBetween
argument_list|(
literal|16
argument_list|,
literal|128
argument_list|)
decl_stmt|;
name|MultiSearchRequest
name|multiSearchRequest
init|=
operator|new
name|MultiSearchRequest
argument_list|()
decl_stmt|;
name|multiSearchRequest
operator|.
name|maxConcurrentSearchRequests
argument_list|(
name|maxAllowedConcurrentSearches
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
name|numSearchRequests
condition|;
name|i
operator|++
control|)
block|{
name|multiSearchRequest
operator|.
name|add
argument_list|(
operator|new
name|SearchRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|MultiSearchResponse
name|response
init|=
name|action
operator|.
name|execute
argument_list|(
name|multiSearchRequest
argument_list|)
operator|.
name|actionGet
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
name|numSearchRequests
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|errorHolder
operator|.
name|get
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|assertTrue
argument_list|(
name|ESTestCase
operator|.
name|terminate
argument_list|(
name|threadPool
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testDefaultMaxConcurrentSearches
specifier|public
name|void
name|testDefaultMaxConcurrentSearches
parameter_list|()
block|{
name|int
name|numDataNodes
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|DiscoveryNodes
operator|.
name|Builder
name|builder
init|=
name|DiscoveryNodes
operator|.
name|builder
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
name|numDataNodes
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|add
argument_list|(
operator|new
name|DiscoveryNode
argument_list|(
literal|"_id"
operator|+
name|i
argument_list|,
operator|new
name|LocalTransportAddress
argument_list|(
literal|"_id"
operator|+
name|i
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|DiscoveryNode
operator|.
name|Role
operator|.
name|DATA
argument_list|)
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|add
argument_list|(
operator|new
name|DiscoveryNode
argument_list|(
literal|"master"
argument_list|,
operator|new
name|LocalTransportAddress
argument_list|(
literal|"mater"
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|DiscoveryNode
operator|.
name|Role
operator|.
name|MASTER
argument_list|)
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|add
argument_list|(
operator|new
name|DiscoveryNode
argument_list|(
literal|"ingest"
argument_list|,
operator|new
name|LocalTransportAddress
argument_list|(
literal|"ingest"
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|DiscoveryNode
operator|.
name|Role
operator|.
name|INGEST
argument_list|)
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
expr_stmt|;
name|ClusterState
name|state
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"_name"
argument_list|)
argument_list|)
operator|.
name|nodes
argument_list|(
name|builder
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|result
init|=
name|TransportMultiSearchAction
operator|.
name|defaultMaxConcurrentSearches
argument_list|(
literal|10
argument_list|,
name|state
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
argument_list|,
name|equalTo
argument_list|(
literal|10
operator|*
name|numDataNodes
argument_list|)
argument_list|)
expr_stmt|;
name|state
operator|=
name|ClusterState
operator|.
name|builder
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"_name"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
name|result
operator|=
name|TransportMultiSearchAction
operator|.
name|defaultMaxConcurrentSearches
argument_list|(
literal|10
argument_list|,
name|state
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

