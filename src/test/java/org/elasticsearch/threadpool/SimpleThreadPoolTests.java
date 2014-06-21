begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.threadpool
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Sets
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
name|node
operator|.
name|info
operator|.
name|NodeInfo
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
name|node
operator|.
name|info
operator|.
name|NodesInfoResponse
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
name|IndexRequestBuilder
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
name|network
operator|.
name|MulticastChannel
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
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
name|json
operator|.
name|JsonXContent
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
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
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
name|InternalTestCluster
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
operator|.
name|Names
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
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ThreadInfo
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ThreadMXBean
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
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|settings
operator|.
name|ImmutableSettings
operator|.
name|settingsBuilder
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
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchIntegrationTest
operator|.
name|Scope
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
name|assertAllSuccessful
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
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|TEST
argument_list|,
name|numDataNodes
operator|=
literal|0
argument_list|,
name|numClientNodes
operator|=
literal|0
argument_list|)
DECL|class|SimpleThreadPoolTests
specifier|public
class|class
name|SimpleThreadPoolTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.type"
argument_list|,
literal|"cached"
argument_list|)
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
DECL|method|verifyThreadNames
specifier|public
name|void
name|verifyThreadNames
parameter_list|()
throws|throws
name|Exception
block|{
name|ThreadMXBean
name|threadBean
init|=
name|ManagementFactory
operator|.
name|getThreadMXBean
argument_list|()
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|preNodeStartThreadNames
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|l
range|:
name|threadBean
operator|.
name|getAllThreadIds
argument_list|()
control|)
block|{
name|ThreadInfo
name|threadInfo
init|=
name|threadBean
operator|.
name|getThreadInfo
argument_list|(
name|l
argument_list|)
decl_stmt|;
if|if
condition|(
name|threadInfo
operator|!=
literal|null
condition|)
block|{
name|preNodeStartThreadNames
operator|.
name|add
argument_list|(
name|threadInfo
operator|.
name|getThreadName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"pre node threads are {}"
argument_list|,
name|preNodeStartThreadNames
argument_list|)
expr_stmt|;
name|String
name|node
init|=
name|internalCluster
argument_list|()
operator|.
name|startNode
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"do some indexing, flushing, optimize, and searches"
argument_list|)
expr_stmt|;
name|int
name|numDocs
init|=
name|randomIntBetween
argument_list|(
literal|2
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|IndexRequestBuilder
index|[]
name|builders
init|=
operator|new
name|IndexRequestBuilder
index|[
name|numDocs
index|]
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
name|numDocs
condition|;
operator|++
name|i
control|)
block|{
name|builders
index|[
name|i
index|]
operator|=
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"idx"
argument_list|,
literal|"type"
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
literal|"str_value"
argument_list|,
literal|"s"
operator|+
name|i
argument_list|)
operator|.
name|field
argument_list|(
literal|"str_values"
argument_list|,
operator|new
name|String
index|[]
block|{
literal|"s"
operator|+
operator|(
name|i
operator|*
literal|2
operator|)
block|,
literal|"s"
operator|+
operator|(
name|i
operator|*
literal|2
operator|+
literal|1
operator|)
block|}
argument_list|)
operator|.
name|field
argument_list|(
literal|"l_value"
argument_list|,
name|i
argument_list|)
operator|.
name|field
argument_list|(
literal|"l_values"
argument_list|,
operator|new
name|int
index|[]
block|{
name|i
operator|*
literal|2
block|,
name|i
operator|*
literal|2
operator|+
literal|1
block|}
argument_list|)
operator|.
name|field
argument_list|(
literal|"d_value"
argument_list|,
name|i
argument_list|)
operator|.
name|field
argument_list|(
literal|"d_values"
argument_list|,
operator|new
name|double
index|[]
block|{
name|i
operator|*
literal|2
block|,
name|i
operator|*
literal|2
operator|+
literal|1
block|}
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|builders
argument_list|)
expr_stmt|;
name|int
name|numSearches
init|=
name|randomIntBetween
argument_list|(
literal|2
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
name|numSearches
condition|;
name|i
operator|++
control|)
block|{
name|assertAllSuccessful
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"str_value"
argument_list|,
literal|"s"
operator|+
name|i
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertAllSuccessful
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"idx"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"l_value"
argument_list|,
name|i
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Set
argument_list|<
name|String
argument_list|>
name|threadNames
init|=
name|Sets
operator|.
name|newHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|long
name|l
range|:
name|threadBean
operator|.
name|getAllThreadIds
argument_list|()
control|)
block|{
name|ThreadInfo
name|threadInfo
init|=
name|threadBean
operator|.
name|getThreadInfo
argument_list|(
name|l
argument_list|)
decl_stmt|;
if|if
condition|(
name|threadInfo
operator|!=
literal|null
condition|)
block|{
name|threadNames
operator|.
name|add
argument_list|(
name|threadInfo
operator|.
name|getThreadName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"post node threads are {}"
argument_list|,
name|threadNames
argument_list|)
expr_stmt|;
name|threadNames
operator|.
name|removeAll
argument_list|(
name|preNodeStartThreadNames
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"post node *new* threads are {}"
argument_list|,
name|threadNames
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|threadName
range|:
name|threadNames
control|)
block|{
comment|// ignore some shared threads we know that are created within the same VM, like the shared discovery one
if|if
condition|(
name|threadName
operator|.
name|contains
argument_list|(
literal|"["
operator|+
name|MulticastChannel
operator|.
name|SHARED_CHANNEL_NAME
operator|+
literal|"]"
argument_list|)
condition|)
block|{
continue|continue;
block|}
name|assertThat
argument_list|(
name|threadName
argument_list|,
name|anyOf
argument_list|(
name|containsString
argument_list|(
literal|"["
operator|+
name|node
operator|+
literal|"]"
argument_list|)
argument_list|,
name|containsString
argument_list|(
literal|"["
operator|+
name|InternalTestCluster
operator|.
name|TRANSPORT_CLIENT_PREFIX
operator|+
name|node
operator|+
literal|"]"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|20000
argument_list|)
DECL|method|testUpdatingThreadPoolSettings
specifier|public
name|void
name|testUpdatingThreadPoolSettings
parameter_list|()
throws|throws
name|Exception
block|{
name|internalCluster
argument_list|()
operator|.
name|startNodesAsync
argument_list|(
literal|2
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ThreadPool
name|threadPool
init|=
name|internalCluster
argument_list|()
operator|.
name|getDataNodeInstance
argument_list|(
name|ThreadPool
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Check that settings are changed
name|assertThat
argument_list|(
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|threadPool
operator|.
name|executor
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
operator|)
operator|.
name|getKeepAliveTime
argument_list|(
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|5L
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.keep_alive"
argument_list|,
literal|"10m"
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
name|assertThat
argument_list|(
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|threadPool
operator|.
name|executor
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
operator|)
operator|.
name|getKeepAliveTime
argument_list|(
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure that threads continue executing when executor is replaced
specifier|final
name|CyclicBarrier
name|barrier
init|=
operator|new
name|CyclicBarrier
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|Executor
name|oldExecutor
init|=
name|threadPool
operator|.
name|executor
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
decl_stmt|;
name|threadPool
operator|.
name|executor
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BrokenBarrierException
name|ex
parameter_list|)
block|{
comment|//
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.type"
argument_list|,
literal|"fixed"
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
name|assertThat
argument_list|(
name|threadPool
operator|.
name|executor
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
argument_list|,
name|not
argument_list|(
name|sameInstance
argument_list|(
name|oldExecutor
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|oldExecutor
operator|)
operator|.
name|isShutdown
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
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|oldExecutor
operator|)
operator|.
name|isTerminating
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
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|oldExecutor
operator|)
operator|.
name|isTerminated
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
comment|// Make sure that new thread executor is functional
name|threadPool
operator|.
name|executor
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|BrokenBarrierException
name|ex
parameter_list|)
block|{
comment|//
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|()
operator|.
name|setTransientSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.type"
argument_list|,
literal|"fixed"
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
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
comment|// Check that node info is correct
name|NodesInfoResponse
name|nodesInfoResponse
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesInfo
argument_list|()
operator|.
name|all
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
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
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|NodeInfo
name|nodeInfo
init|=
name|nodesInfoResponse
operator|.
name|getNodes
argument_list|()
index|[
name|i
index|]
decl_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|ThreadPool
operator|.
name|Info
name|info
range|:
name|nodeInfo
operator|.
name|getThreadPool
argument_list|()
control|)
block|{
if|if
condition|(
name|info
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|info
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"fixed"
argument_list|)
argument_list|)
expr_stmt|;
name|found
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
name|assertThat
argument_list|(
name|found
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|poolMap
init|=
name|getPoolSettingsThroughJson
argument_list|(
name|nodeInfo
operator|.
name|getThreadPool
argument_list|()
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
decl_stmt|;
block|}
block|}
DECL|method|getPoolSettingsThroughJson
specifier|private
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getPoolSettingsThroughJson
parameter_list|(
name|ThreadPoolInfo
name|info
parameter_list|,
name|String
name|poolName
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|info
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
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|close
argument_list|()
expr_stmt|;
name|XContentParser
name|parser
init|=
name|JsonXContent
operator|.
name|jsonXContent
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|string
argument_list|()
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|poolsMap
init|=
name|parser
operator|.
name|mapAndClose
argument_list|()
decl_stmt|;
return|return
call|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
call|)
argument_list|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|poolsMap
operator|.
name|get
argument_list|(
literal|"thread_pool"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|(
name|poolName
argument_list|)
return|;
block|}
block|}
end_class

end_unit

