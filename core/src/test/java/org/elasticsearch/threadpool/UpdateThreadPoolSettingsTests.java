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
name|util
operator|.
name|concurrent
operator|.
name|MoreExecutors
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
name|util
operator|.
name|concurrent
operator|.
name|EsThreadPoolExecutor
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
name|lang
operator|.
name|reflect
operator|.
name|Field
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
name|CountDownLatch
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
name|Executor
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
name|ThreadPoolExecutor
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
name|TimeUnit
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
name|Settings
operator|.
name|settingsBuilder
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
DECL|class|UpdateThreadPoolSettingsTests
specifier|public
class|class
name|UpdateThreadPoolSettingsTests
extends|extends
name|ElasticsearchTestCase
block|{
DECL|method|info
specifier|private
name|ThreadPool
operator|.
name|Info
name|info
parameter_list|(
name|ThreadPool
name|threadPool
parameter_list|,
name|String
name|name
parameter_list|)
block|{
for|for
control|(
name|ThreadPool
operator|.
name|Info
name|info
range|:
name|threadPool
operator|.
name|info
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
name|name
argument_list|)
condition|)
block|{
return|return
name|info
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Test
DECL|method|testCachedExecutorType
specifier|public
name|void
name|testCachedExecutorType
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|(
name|Settings
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
literal|"name"
argument_list|,
literal|"testCachedExecutorType"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cached"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getKeepAlive
argument_list|()
operator|.
name|minutes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5L
argument_list|)
argument_list|)
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
name|instanceOf
argument_list|(
name|EsThreadPoolExecutor
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// Replace with different type
name|threadPool
operator|.
name|updateSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.type"
argument_list|,
literal|"same"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"same"
argument_list|)
argument_list|)
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
name|instanceOf
argument_list|(
name|MoreExecutors
operator|.
name|directExecutor
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Replace with different type again
name|threadPool
operator|.
name|updateSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.type"
argument_list|,
literal|"scaling"
argument_list|)
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
expr_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"scaling"
argument_list|)
argument_list|)
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
name|instanceOf
argument_list|(
name|EsThreadPoolExecutor
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|EsThreadPoolExecutor
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
name|getCorePoolSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure keep alive value changed
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getKeepAlive
argument_list|()
operator|.
name|minutes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|EsThreadPoolExecutor
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
comment|// Put old type back
name|threadPool
operator|.
name|updateSettings
argument_list|(
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
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cached"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure keep alive value reused
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getKeepAlive
argument_list|()
operator|.
name|minutes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10L
argument_list|)
argument_list|)
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
name|instanceOf
argument_list|(
name|EsThreadPoolExecutor
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// Change keep alive
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
name|updateSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.keep_alive"
argument_list|,
literal|"1m"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// Make sure keep alive value changed
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getKeepAlive
argument_list|()
operator|.
name|minutes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|EsThreadPoolExecutor
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
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure executor didn't change
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cached"
argument_list|)
argument_list|)
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
name|sameInstance
argument_list|(
name|oldExecutor
argument_list|)
argument_list|)
expr_stmt|;
comment|// Set the same keep alive
name|threadPool
operator|.
name|updateSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.keep_alive"
argument_list|,
literal|"1m"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// Make sure keep alive value didn't change
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getKeepAlive
argument_list|()
operator|.
name|minutes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|EsThreadPoolExecutor
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
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure executor didn't change
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cached"
argument_list|)
argument_list|)
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
name|sameInstance
argument_list|(
name|oldExecutor
argument_list|)
argument_list|)
expr_stmt|;
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testFixedExecutorType
specifier|public
name|void
name|testFixedExecutorType
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
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
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"testCachedExecutorType"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
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
name|instanceOf
argument_list|(
name|EsThreadPoolExecutor
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// Replace with different type
name|threadPool
operator|.
name|updateSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.type"
argument_list|,
literal|"scaling"
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.search.keep_alive"
argument_list|,
literal|"10m"
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.search.min"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.search.size"
argument_list|,
literal|"15"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"scaling"
argument_list|)
argument_list|)
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
name|instanceOf
argument_list|(
name|EsThreadPoolExecutor
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|EsThreadPoolExecutor
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
name|getCorePoolSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|EsThreadPoolExecutor
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
name|getMaximumPoolSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|15
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getMin
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getMax
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|15
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure keep alive value changed
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getKeepAlive
argument_list|()
operator|.
name|minutes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|EsThreadPoolExecutor
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
comment|// Put old type back
name|threadPool
operator|.
name|updateSettings
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
expr_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
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
comment|// Make sure keep alive value is not used
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getKeepAlive
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// Make sure keep pool size value were reused
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getMin
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|15
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getMax
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|15
argument_list|)
argument_list|)
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
name|instanceOf
argument_list|(
name|EsThreadPoolExecutor
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|EsThreadPoolExecutor
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
name|getCorePoolSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|15
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|EsThreadPoolExecutor
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
name|getMaximumPoolSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|15
argument_list|)
argument_list|)
expr_stmt|;
comment|// Change size
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
name|updateSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.size"
argument_list|,
literal|"10"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
comment|// Make sure size values changed
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getMax
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
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getMin
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
operator|(
operator|(
name|EsThreadPoolExecutor
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
name|getMaximumPoolSize
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
operator|(
operator|(
name|EsThreadPoolExecutor
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
name|getCorePoolSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure executor didn't change
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
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
name|sameInstance
argument_list|(
name|oldExecutor
argument_list|)
argument_list|)
expr_stmt|;
comment|// Change queue capacity
name|threadPool
operator|.
name|updateSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.queue"
argument_list|,
literal|"500"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testScalingExecutorType
specifier|public
name|void
name|testScalingExecutorType
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.type"
argument_list|,
literal|"scaling"
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.search.size"
argument_list|,
literal|10
argument_list|)
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"testCachedExecutorType"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getMin
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
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getMax
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
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getKeepAlive
argument_list|()
operator|.
name|minutes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"scaling"
argument_list|)
argument_list|)
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
name|instanceOf
argument_list|(
name|EsThreadPoolExecutor
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// Change settings that doesn't require pool replacement
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
name|updateSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.search.type"
argument_list|,
literal|"scaling"
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.search.keep_alive"
argument_list|,
literal|"10m"
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.search.min"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.search.size"
argument_list|,
literal|"15"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"scaling"
argument_list|)
argument_list|)
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
name|instanceOf
argument_list|(
name|EsThreadPoolExecutor
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|EsThreadPoolExecutor
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
name|getCorePoolSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|EsThreadPoolExecutor
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
name|getMaximumPoolSize
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|15
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getMin
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getMax
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|15
argument_list|)
argument_list|)
expr_stmt|;
comment|// Make sure keep alive value changed
name|assertThat
argument_list|(
name|info
argument_list|(
name|threadPool
argument_list|,
name|Names
operator|.
name|SEARCH
argument_list|)
operator|.
name|getKeepAlive
argument_list|()
operator|.
name|minutes
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|EsThreadPoolExecutor
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
name|sameInstance
argument_list|(
name|oldExecutor
argument_list|)
argument_list|)
expr_stmt|;
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|10000
argument_list|)
DECL|method|testShutdownDownNowDoesntBlock
specifier|public
name|void
name|testShutdownDownNowDoesntBlock
parameter_list|()
throws|throws
name|Exception
block|{
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|(
name|Settings
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
literal|"name"
argument_list|,
literal|"testCachedExecutorType"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
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
name|Thread
operator|.
name|sleep
argument_list|(
literal|20000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|threadPool
operator|.
name|updateSettings
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
name|threadPool
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
comment|// interrupt the thread
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testCustomThreadPool
specifier|public
name|void
name|testCustomThreadPool
parameter_list|()
throws|throws
name|Exception
block|{
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|(
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"threadpool.my_pool1.type"
argument_list|,
literal|"cached"
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.my_pool2.type"
argument_list|,
literal|"fixed"
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.my_pool2.size"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.my_pool2.queue_size"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|put
argument_list|(
literal|"name"
argument_list|,
literal|"testCustomThreadPool"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|ThreadPoolInfo
name|groups
init|=
name|threadPool
operator|.
name|info
argument_list|()
decl_stmt|;
name|boolean
name|foundPool1
init|=
literal|false
decl_stmt|;
name|boolean
name|foundPool2
init|=
literal|false
decl_stmt|;
name|outer
label|:
for|for
control|(
name|ThreadPool
operator|.
name|Info
name|info
range|:
name|groups
control|)
block|{
if|if
condition|(
literal|"my_pool1"
operator|.
name|equals
argument_list|(
name|info
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|foundPool1
operator|=
literal|true
expr_stmt|;
name|assertThat
argument_list|(
name|info
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cached"
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"my_pool2"
operator|.
name|equals
argument_list|(
name|info
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|foundPool2
operator|=
literal|true
expr_stmt|;
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
name|assertThat
argument_list|(
name|info
operator|.
name|getMin
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
name|info
operator|.
name|getMax
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
name|info
operator|.
name|getQueueSize
argument_list|()
operator|.
name|singles
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|Field
name|field
range|:
name|Names
operator|.
name|class
operator|.
name|getFields
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
name|equalsIgnoreCase
argument_list|(
name|field
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
comment|// This is ok it is a default thread pool
continue|continue
name|outer
continue|;
block|}
block|}
name|fail
argument_list|(
literal|"Unexpected pool name: "
operator|+
name|info
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|foundPool1
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|foundPool2
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
comment|// Updating my_pool2
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
literal|"threadpool.my_pool2.size"
argument_list|,
literal|"10"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|threadPool
operator|.
name|updateSettings
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|groups
operator|=
name|threadPool
operator|.
name|info
argument_list|()
expr_stmt|;
name|foundPool1
operator|=
literal|false
expr_stmt|;
name|foundPool2
operator|=
literal|false
expr_stmt|;
name|outer
label|:
for|for
control|(
name|ThreadPool
operator|.
name|Info
name|info
range|:
name|groups
control|)
block|{
if|if
condition|(
literal|"my_pool1"
operator|.
name|equals
argument_list|(
name|info
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|foundPool1
operator|=
literal|true
expr_stmt|;
name|assertThat
argument_list|(
name|info
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cached"
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"my_pool2"
operator|.
name|equals
argument_list|(
name|info
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
name|foundPool2
operator|=
literal|true
expr_stmt|;
name|assertThat
argument_list|(
name|info
operator|.
name|getMax
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
name|info
operator|.
name|getMin
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
name|info
operator|.
name|getQueueSize
argument_list|()
operator|.
name|singles
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
block|}
else|else
block|{
for|for
control|(
name|Field
name|field
range|:
name|Names
operator|.
name|class
operator|.
name|getFields
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
name|equalsIgnoreCase
argument_list|(
name|field
operator|.
name|getName
argument_list|()
argument_list|)
condition|)
block|{
comment|// This is ok it is a default thread pool
continue|continue
name|outer
continue|;
block|}
block|}
name|fail
argument_list|(
literal|"Unexpected pool name: "
operator|+
name|info
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|foundPool1
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|foundPool2
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|terminate
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
