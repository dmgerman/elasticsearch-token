begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|recovery
package|;
end_package

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
name|logging
operator|.
name|ESLogger
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
name|logging
operator|.
name|Loggers
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
name|Test
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
name|atomic
operator|.
name|AtomicBoolean
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
name|AtomicLong
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
DECL|class|RecoveryWhileUnderLoadTests
specifier|public
class|class
name|RecoveryWhileUnderLoadTests
extends|extends
name|AbstractNodesTests
block|{
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|RecoveryWhileUnderLoadTests
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|method|shutdownNodes
annotation|@
name|AfterMethod
specifier|public
name|void
name|shutdownNodes
parameter_list|()
block|{
name|closeAllNodes
argument_list|()
expr_stmt|;
block|}
DECL|method|recoverWhileUnderLoadAllocateBackupsTest
annotation|@
name|Test
specifier|public
name|void
name|recoverWhileUnderLoadAllocateBackupsTest
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting [node1] ..."
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"node1"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> creating test index ..."
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
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
specifier|final
name|AtomicLong
name|idGenerator
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|final
name|AtomicLong
name|indexCounter
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|final
name|AtomicBoolean
name|stop
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|Thread
index|[]
name|writers
init|=
operator|new
name|Thread
index|[
literal|5
index|]
decl_stmt|;
specifier|final
name|CountDownLatch
name|stopLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|writers
operator|.
name|length
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting {} indexing threads"
argument_list|,
name|writers
operator|.
name|length
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
name|writers
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|indexerId
init|=
name|i
decl_stmt|;
name|writers
index|[
name|i
index|]
operator|=
operator|new
name|Thread
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
name|logger
operator|.
name|info
argument_list|(
literal|"**** starting indexing thread {}"
argument_list|,
name|indexerId
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
name|long
name|id
init|=
name|idGenerator
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|id
operator|%
literal|1000
operator|==
literal|0
condition|)
block|{
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|id
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
name|MapBuilder
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|newMapBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"test"
argument_list|,
literal|"value"
operator|+
name|id
argument_list|)
operator|.
name|map
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|indexCounter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"**** done indexing thread {}"
argument_list|,
name|indexerId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"**** failed indexing thread {}"
argument_list|,
name|e
argument_list|,
name|indexerId
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|stopLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
expr_stmt|;
name|writers
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for 2000 docs to be indexed ..."
argument_list|)
expr_stmt|;
while|while
condition|(
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
operator|<
literal|2000
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> 2000 docs indexed"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> flushing the index ...."
argument_list|)
expr_stmt|;
comment|// now flush, just to make sure we have some data in the index, not just translog
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for 4000 docs to be indexed ..."
argument_list|)
expr_stmt|;
while|while
condition|(
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
operator|<
literal|4000
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> 4000 docs indexed"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting [node2] ..."
argument_list|)
expr_stmt|;
comment|// now start another node, while we index
name|startNode
argument_list|(
literal|"node2"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for GREEN health status ..."
argument_list|)
expr_stmt|;
comment|// make sure the cluster state is green, and all has been recovered
name|assertThat
argument_list|(
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setTimeout
argument_list|(
literal|"1m"
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"2"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for 10000 docs to be indexed ..."
argument_list|)
expr_stmt|;
while|while
condition|(
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
operator|<
literal|10000
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> 10000 docs indexed"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> marking and waiting for indexing threads to stop ..."
argument_list|)
expr_stmt|;
name|stop
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|stopLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> indexing threads stopped"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> refreshing the index"
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> verifying indexed content"
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|indexCounter
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|recoverWhileUnderLoadAllocateBackupsRelocatePrimariesTest
annotation|@
name|Test
specifier|public
name|void
name|recoverWhileUnderLoadAllocateBackupsRelocatePrimariesTest
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting [node1] ..."
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"node1"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> creating test index ..."
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
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
specifier|final
name|AtomicLong
name|idGenerator
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|final
name|AtomicLong
name|indexCounter
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|final
name|AtomicBoolean
name|stop
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|Thread
index|[]
name|writers
init|=
operator|new
name|Thread
index|[
literal|5
index|]
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting {} indexing threads"
argument_list|,
name|writers
operator|.
name|length
argument_list|)
expr_stmt|;
specifier|final
name|CountDownLatch
name|stopLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|writers
operator|.
name|length
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
name|writers
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|indexerId
init|=
name|i
decl_stmt|;
name|writers
index|[
name|i
index|]
operator|=
operator|new
name|Thread
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
name|logger
operator|.
name|info
argument_list|(
literal|"**** starting indexing thread {}"
argument_list|,
name|indexerId
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
name|long
name|id
init|=
name|idGenerator
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|id
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
name|MapBuilder
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|newMapBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"test"
argument_list|,
literal|"value"
operator|+
name|id
argument_list|)
operator|.
name|map
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|indexCounter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"**** done indexing thread {}"
argument_list|,
name|indexerId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"**** failed indexing thread {}"
argument_list|,
name|e
argument_list|,
name|indexerId
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|stopLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
expr_stmt|;
name|writers
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for 2000 docs to be indexed ..."
argument_list|)
expr_stmt|;
while|while
condition|(
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
operator|<
literal|2000
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> 2000 docs indexed"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> flushing the index ...."
argument_list|)
expr_stmt|;
comment|// now flush, just to make sure we have some data in the index, not just translog
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for 4000 docs to be indexed ..."
argument_list|)
expr_stmt|;
while|while
condition|(
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
operator|<
literal|4000
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> 4000 docs indexed"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting [node2] ..."
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"node2"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting [node3] ..."
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"node3"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting [node4] ..."
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"node4"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for GREEN health status ..."
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setTimeout
argument_list|(
literal|"1m"
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"4"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for 15000 docs to be indexed ..."
argument_list|)
expr_stmt|;
while|while
condition|(
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
operator|<
literal|15000
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> 15000 docs indexed"
argument_list|)
expr_stmt|;
name|stop
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|stopLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> marking and waiting for indexing threads to stop ..."
argument_list|)
expr_stmt|;
name|stop
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|stopLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> indexing threads stopped"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> refreshing the index"
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> verifying indexed content"
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|indexCounter
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|recoverWhileUnderLoadWithNodeShutdown
annotation|@
name|Test
specifier|public
name|void
name|recoverWhileUnderLoadWithNodeShutdown
parameter_list|()
throws|throws
name|Exception
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting [node1] ..."
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"node1"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting [node2] ..."
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"node2"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> creating test index ..."
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
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
specifier|final
name|AtomicLong
name|idGenerator
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|final
name|AtomicLong
name|indexCounter
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
specifier|final
name|AtomicBoolean
name|stop
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|Thread
index|[]
name|writers
init|=
operator|new
name|Thread
index|[
literal|5
index|]
decl_stmt|;
specifier|final
name|CountDownLatch
name|stopLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|writers
operator|.
name|length
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting {} indexing threads"
argument_list|,
name|writers
operator|.
name|length
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
name|writers
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|int
name|indexerId
init|=
name|i
decl_stmt|;
name|writers
index|[
name|i
index|]
operator|=
operator|new
name|Thread
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
name|logger
operator|.
name|info
argument_list|(
literal|"**** starting indexing thread {}"
argument_list|,
name|indexerId
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|stop
operator|.
name|get
argument_list|()
condition|)
block|{
name|long
name|id
init|=
name|idGenerator
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|client
argument_list|(
literal|"node2"
argument_list|)
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|Long
operator|.
name|toString
argument_list|(
name|id
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
name|MapBuilder
operator|.
expr|<
name|String
argument_list|,
name|Object
operator|>
name|newMapBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"test"
argument_list|,
literal|"value"
operator|+
name|id
argument_list|)
operator|.
name|map
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|indexCounter
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"**** done indexing thread {}"
argument_list|,
name|indexerId
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"**** failed indexing thread {}"
argument_list|,
name|e
argument_list|,
name|indexerId
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|stopLatch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
expr_stmt|;
name|writers
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for 2000 docs to be indexed ..."
argument_list|)
expr_stmt|;
while|while
condition|(
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
operator|<
literal|2000
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> 2000 docs indexed"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> flushing the index ...."
argument_list|)
expr_stmt|;
comment|// now flush, just to make sure we have some data in the index, not just translog
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for 4000 docs to be indexed ..."
argument_list|)
expr_stmt|;
while|while
condition|(
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
operator|<
literal|4000
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> 4000 docs indexed"
argument_list|)
expr_stmt|;
comment|// now start more nodes, while we index
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting [node3] ..."
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"node3"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> starting [node4] ..."
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"node4"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for GREEN health status ..."
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setTimeout
argument_list|(
literal|"1m"
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"4"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for 10000 docs to be indexed ..."
argument_list|)
expr_stmt|;
while|while
condition|(
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
operator|<
literal|10000
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node1"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"--> 10000 docs indexed"
argument_list|)
expr_stmt|;
comment|// now, shutdown nodes
name|logger
operator|.
name|info
argument_list|(
literal|"--> shutting down [node1] ..."
argument_list|)
expr_stmt|;
name|closeNode
argument_list|(
literal|"node1"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for GREEN health status ..."
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|(
literal|"node2"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setTimeout
argument_list|(
literal|"1m"
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"3"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> shutting down [node3] ..."
argument_list|)
expr_stmt|;
name|closeNode
argument_list|(
literal|"node3"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for GREEN health status ..."
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|(
literal|"node2"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setTimeout
argument_list|(
literal|"1m"
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"2"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> shutting down [node4] ..."
argument_list|)
expr_stmt|;
name|closeNode
argument_list|(
literal|"node4"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for YELLOW health status ..."
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|(
literal|"node2"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setTimeout
argument_list|(
literal|"1m"
argument_list|)
operator|.
name|setWaitForYellowStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> marking and waiting for indexing threads to stop ..."
argument_list|)
expr_stmt|;
name|stop
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|stopLatch
operator|.
name|await
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> indexing threads stopped"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|(
literal|"node2"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setTimeout
argument_list|(
literal|"1m"
argument_list|)
operator|.
name|setWaitForYellowStatus
argument_list|()
operator|.
name|setWaitForNodes
argument_list|(
literal|"1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
name|logger
operator|.
name|info
argument_list|(
literal|"--> refreshing the index"
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node2"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> verifying indexed content"
argument_list|)
expr_stmt|;
name|client
argument_list|(
literal|"node2"
argument_list|)
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
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
literal|10
condition|;
name|i
operator|++
control|)
block|{
name|assertThat
argument_list|(
name|client
argument_list|(
literal|"node2"
argument_list|)
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|indexCounter
operator|.
name|get
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

