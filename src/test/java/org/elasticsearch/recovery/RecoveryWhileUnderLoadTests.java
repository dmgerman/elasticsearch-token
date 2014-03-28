begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.recovery
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|recovery
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
name|base
operator|.
name|Predicate
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|LuceneTestCase
operator|.
name|Slow
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
name|admin
operator|.
name|indices
operator|.
name|stats
operator|.
name|IndicesStatsResponse
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
name|stats
operator|.
name|ShardStats
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
name|search
operator|.
name|SearchType
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
name|Priority
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
name|index
operator|.
name|shard
operator|.
name|DocsStats
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
name|BackgroundIndexer
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
name|junit
operator|.
name|annotations
operator|.
name|TestLogging
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
name|Arrays
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
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
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
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|matchAllQuery
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
name|equalTo
import|;
end_import

begin_class
DECL|class|RecoveryWhileUnderLoadTests
specifier|public
class|class
name|RecoveryWhileUnderLoadTests
extends|extends
name|ElasticsearchIntegrationTest
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
annotation|@
name|Test
annotation|@
name|TestLogging
argument_list|(
literal|"action.search.type:TRACE,action.admin.indices.refresh:TRACE"
argument_list|)
annotation|@
name|Slow
DECL|method|recoverWhileUnderLoadAllocateBackupsTest
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
literal|"--> creating test index ..."
argument_list|)
expr_stmt|;
name|int
name|numberOfShards
init|=
name|numberOfShards
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|,
literal|1
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|numberOfShards
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|int
name|totalNumDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|200
argument_list|,
literal|20000
argument_list|)
decl_stmt|;
try|try
init|(
name|BackgroundIndexer
name|indexer
init|=
operator|new
name|BackgroundIndexer
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
name|client
argument_list|()
argument_list|)
init|)
block|{
name|int
name|waitFor
init|=
name|totalNumDocs
operator|/
literal|10
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for {} docs to be indexed ..."
argument_list|,
name|waitFor
argument_list|)
expr_stmt|;
name|waitForDocs
argument_list|(
name|waitFor
argument_list|,
name|indexer
argument_list|)
expr_stmt|;
name|indexer
operator|.
name|assertNoFailures
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> {} docs indexed"
argument_list|,
name|waitFor
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
argument_list|()
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
name|waitFor
operator|+=
name|totalNumDocs
operator|/
literal|10
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for {} docs to be indexed ..."
argument_list|,
name|waitFor
argument_list|)
expr_stmt|;
name|waitForDocs
argument_list|(
name|waitFor
argument_list|,
name|indexer
argument_list|)
expr_stmt|;
name|indexer
operator|.
name|assertNoFailures
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> {} docs indexed"
argument_list|,
name|waitFor
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> allow 2 nodes for index [test] ..."
argument_list|)
expr_stmt|;
comment|// now start another node, while we index
name|allowNodes
argument_list|(
literal|"test"
argument_list|,
literal|2
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
argument_list|()
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
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
literal|">=2"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isTimedOut
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
literal|"--> waiting for {} docs to be indexed ..."
argument_list|,
name|totalNumDocs
argument_list|)
expr_stmt|;
name|waitForDocs
argument_list|(
name|totalNumDocs
argument_list|,
name|indexer
argument_list|)
expr_stmt|;
name|indexer
operator|.
name|assertNoFailures
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> {} docs indexed"
argument_list|,
name|totalNumDocs
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> marking and waiting for indexing threads to stop ..."
argument_list|)
expr_stmt|;
name|indexer
operator|.
name|stop
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
name|refreshAndAssert
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> verifying indexed content"
argument_list|)
expr_stmt|;
name|iterateAssertCount
argument_list|(
name|numberOfShards
argument_list|,
name|indexer
operator|.
name|totalIndexedDocs
argument_list|()
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
annotation|@
name|TestLogging
argument_list|(
literal|"action.search.type:TRACE,action.admin.indices.refresh:TRACE"
argument_list|)
annotation|@
name|Slow
DECL|method|recoverWhileUnderLoadAllocateBackupsRelocatePrimariesTest
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
literal|"--> creating test index ..."
argument_list|)
expr_stmt|;
name|int
name|numberOfShards
init|=
name|numberOfShards
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|,
literal|1
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|numberOfShards
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|int
name|totalNumDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|200
argument_list|,
literal|20000
argument_list|)
decl_stmt|;
try|try
init|(
name|BackgroundIndexer
name|indexer
init|=
operator|new
name|BackgroundIndexer
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
name|client
argument_list|()
argument_list|)
init|)
block|{
name|int
name|waitFor
init|=
name|totalNumDocs
operator|/
literal|10
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for {} docs to be indexed ..."
argument_list|,
name|waitFor
argument_list|)
expr_stmt|;
name|waitForDocs
argument_list|(
name|waitFor
argument_list|,
name|indexer
argument_list|)
expr_stmt|;
name|indexer
operator|.
name|assertNoFailures
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> {} docs indexed"
argument_list|,
name|waitFor
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
argument_list|()
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
name|waitFor
operator|+=
name|totalNumDocs
operator|/
literal|10
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for {} docs to be indexed ..."
argument_list|,
name|waitFor
argument_list|)
expr_stmt|;
name|waitForDocs
argument_list|(
name|waitFor
argument_list|,
name|indexer
argument_list|)
expr_stmt|;
name|indexer
operator|.
name|assertNoFailures
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> {} docs indexed"
argument_list|,
name|waitFor
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> allow 4 nodes for index [test] ..."
argument_list|)
expr_stmt|;
name|allowNodes
argument_list|(
literal|"test"
argument_list|,
literal|4
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
argument_list|()
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
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
literal|">=4"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isTimedOut
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
literal|"--> waiting for {} docs to be indexed ..."
argument_list|,
name|totalNumDocs
argument_list|)
expr_stmt|;
name|waitForDocs
argument_list|(
name|totalNumDocs
argument_list|,
name|indexer
argument_list|)
expr_stmt|;
name|indexer
operator|.
name|assertNoFailures
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> {} docs indexed"
argument_list|,
name|totalNumDocs
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> marking and waiting for indexing threads to stop ..."
argument_list|)
expr_stmt|;
name|indexer
operator|.
name|stop
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
name|refreshAndAssert
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> verifying indexed content"
argument_list|)
expr_stmt|;
name|iterateAssertCount
argument_list|(
name|numberOfShards
argument_list|,
name|indexer
operator|.
name|totalIndexedDocs
argument_list|()
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
annotation|@
name|TestLogging
argument_list|(
literal|"action.search.type:TRACE,action.admin.indices.refresh:TRACE"
argument_list|)
annotation|@
name|Slow
DECL|method|recoverWhileUnderLoadWithNodeShutdown
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
literal|"--> creating test index ..."
argument_list|)
expr_stmt|;
name|int
name|numberOfShards
init|=
name|numberOfShards
argument_list|()
decl_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|,
literal|2
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|numberOfShards
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|int
name|totalNumDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|200
argument_list|,
literal|20000
argument_list|)
decl_stmt|;
try|try
init|(
name|BackgroundIndexer
name|indexer
init|=
operator|new
name|BackgroundIndexer
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
name|client
argument_list|()
argument_list|)
init|)
block|{
name|int
name|waitFor
init|=
name|totalNumDocs
operator|/
literal|10
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for {} docs to be indexed ..."
argument_list|,
name|waitFor
argument_list|)
expr_stmt|;
name|waitForDocs
argument_list|(
name|waitFor
argument_list|,
name|indexer
argument_list|)
expr_stmt|;
name|indexer
operator|.
name|assertNoFailures
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> {} docs indexed"
argument_list|,
name|waitFor
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
argument_list|()
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
name|waitFor
operator|+=
name|totalNumDocs
operator|/
literal|10
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for {} docs to be indexed ..."
argument_list|,
name|waitFor
argument_list|)
expr_stmt|;
name|waitForDocs
argument_list|(
name|waitFor
argument_list|,
name|indexer
argument_list|)
expr_stmt|;
name|indexer
operator|.
name|assertNoFailures
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> {} docs indexed"
argument_list|,
name|waitFor
argument_list|)
expr_stmt|;
comment|// now start more nodes, while we index
name|logger
operator|.
name|info
argument_list|(
literal|"--> allow 4 nodes for index [test] ..."
argument_list|)
expr_stmt|;
name|allowNodes
argument_list|(
literal|"test"
argument_list|,
literal|4
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
argument_list|()
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
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
literal|">=4"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isTimedOut
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
literal|"--> waiting for {} docs to be indexed ..."
argument_list|,
name|totalNumDocs
argument_list|)
expr_stmt|;
name|waitForDocs
argument_list|(
name|totalNumDocs
argument_list|,
name|indexer
argument_list|)
expr_stmt|;
name|indexer
operator|.
name|assertNoFailures
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> {} docs indexed"
argument_list|,
name|totalNumDocs
argument_list|)
expr_stmt|;
comment|// now, shutdown nodes
name|logger
operator|.
name|info
argument_list|(
literal|"--> allow 3 nodes for index [test] ..."
argument_list|)
expr_stmt|;
name|allowNodes
argument_list|(
literal|"test"
argument_list|,
literal|3
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
argument_list|()
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
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
literal|">=3"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isTimedOut
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
literal|"--> allow 2 nodes for index [test] ..."
argument_list|)
expr_stmt|;
name|allowNodes
argument_list|(
literal|"test"
argument_list|,
literal|2
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
argument_list|()
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
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
literal|">=2"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isTimedOut
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
literal|"--> allow 1 nodes for index [test] ..."
argument_list|)
expr_stmt|;
name|allowNodes
argument_list|(
literal|"test"
argument_list|,
literal|1
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
argument_list|()
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
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
literal|">=1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isTimedOut
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
name|indexer
operator|.
name|stop
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
argument_list|()
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
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
literal|">=1"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isTimedOut
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
name|refreshAndAssert
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> verifying indexed content"
argument_list|)
expr_stmt|;
name|iterateAssertCount
argument_list|(
name|numberOfShards
argument_list|,
name|indexer
operator|.
name|totalIndexedDocs
argument_list|()
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
annotation|@
name|TestLogging
argument_list|(
literal|"action.search.type:TRACE,action.admin.indices.refresh:TRACE,action.index:TRACE,action.support.replication:TRACE,cluster.service:DEBUG"
argument_list|)
annotation|@
name|Slow
DECL|method|recoverWhileRelocating
specifier|public
name|void
name|recoverWhileRelocating
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|int
name|numShards
init|=
name|between
argument_list|(
literal|2
argument_list|,
literal|10
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numReplicas
init|=
literal|0
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> creating test index ..."
argument_list|)
expr_stmt|;
name|int
name|allowNodes
init|=
literal|2
decl_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|,
literal|3
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
name|numShards
argument_list|)
operator|.
name|put
argument_list|(
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
name|numReplicas
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|int
name|numDocs
init|=
name|scaledRandomIntBetween
argument_list|(
literal|200
argument_list|,
literal|50000
argument_list|)
decl_stmt|;
try|try
init|(
name|BackgroundIndexer
name|indexer
init|=
operator|new
name|BackgroundIndexer
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
name|client
argument_list|()
argument_list|)
init|)
block|{
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
name|i
operator|+=
name|scaledRandomIntBetween
argument_list|(
literal|100
argument_list|,
name|Math
operator|.
name|min
argument_list|(
literal|1000
argument_list|,
name|numDocs
argument_list|)
argument_list|)
control|)
block|{
name|indexer
operator|.
name|assertNoFailures
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> waiting for {} docs to be indexed ..."
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|waitForDocs
argument_list|(
name|i
argument_list|,
name|indexer
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> {} docs indexed"
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|allowNodes
operator|=
literal|2
operator|/
name|allowNodes
expr_stmt|;
name|allowNodes
argument_list|(
literal|"test"
argument_list|,
name|allowNodes
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
argument_list|()
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setTimeout
argument_list|(
literal|"1m"
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isTimedOut
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
literal|"--> marking and waiting for indexing threads to stop ..."
argument_list|)
expr_stmt|;
name|indexer
operator|.
name|stop
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
literal|"--> bump up number of replicas to 1 and allow all nodes to hold the index"
argument_list|)
expr_stmt|;
name|allowNodes
argument_list|(
literal|"test"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareUpdateSettings
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSettings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"number_of_replicas"
argument_list|,
literal|1
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
argument_list|()
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
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setTimeout
argument_list|(
literal|"1m"
argument_list|)
operator|.
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|isTimedOut
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
name|refreshAndAssert
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"--> verifying indexed content"
argument_list|)
expr_stmt|;
name|iterateAssertCount
argument_list|(
name|numShards
argument_list|,
name|indexer
operator|.
name|totalIndexedDocs
argument_list|()
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|iterateAssertCount
specifier|private
name|void
name|iterateAssertCount
parameter_list|(
specifier|final
name|int
name|numberOfShards
parameter_list|,
specifier|final
name|long
name|numberOfDocs
parameter_list|,
specifier|final
name|int
name|iterations
parameter_list|)
throws|throws
name|Exception
block|{
name|SearchResponse
index|[]
name|iterationResults
init|=
operator|new
name|SearchResponse
index|[
name|iterations
index|]
decl_stmt|;
name|boolean
name|error
init|=
literal|false
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
name|iterations
condition|;
name|i
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSearchType
argument_list|(
name|SearchType
operator|.
name|COUNT
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|logSearchResponse
argument_list|(
name|numberOfShards
argument_list|,
name|numberOfDocs
argument_list|,
name|i
argument_list|,
name|searchResponse
argument_list|)
expr_stmt|;
name|iterationResults
index|[
name|i
index|]
operator|=
name|searchResponse
expr_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
operator|!=
name|numberOfDocs
condition|)
block|{
name|error
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
name|error
condition|)
block|{
comment|//Printing out shards and their doc count
name|IndicesStatsResponse
name|indicesStatsResponse
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
name|prepareStats
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
for|for
control|(
name|ShardStats
name|shardStats
range|:
name|indicesStatsResponse
operator|.
name|getShards
argument_list|()
control|)
block|{
name|DocsStats
name|docsStats
init|=
name|shardStats
operator|.
name|getStats
argument_list|()
operator|.
name|docs
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"shard [{}] - count {}, primary {}"
argument_list|,
name|shardStats
operator|.
name|getShardId
argument_list|()
argument_list|,
name|docsStats
operator|.
name|getCount
argument_list|()
argument_list|,
name|shardStats
operator|.
name|getShardRouting
argument_list|()
operator|.
name|primary
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|//if there was an error we try to wait and see if at some point it'll get fixed
name|logger
operator|.
name|info
argument_list|(
literal|"--> trying to wait"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|apply
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|boolean
name|error
init|=
literal|false
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
name|iterations
condition|;
name|i
operator|++
control|)
block|{
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSearchType
argument_list|(
name|SearchType
operator|.
name|COUNT
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
operator|!=
name|numberOfDocs
condition|)
block|{
name|error
operator|=
literal|true
expr_stmt|;
block|}
block|}
return|return
operator|!
name|error
return|;
block|}
block|}
argument_list|,
literal|5
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//lets now make the test fail if it was supposed to fail
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|iterations
condition|;
name|i
operator|++
control|)
block|{
name|assertHitCount
argument_list|(
name|iterationResults
index|[
name|i
index|]
argument_list|,
name|numberOfDocs
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|logSearchResponse
specifier|private
name|void
name|logSearchResponse
parameter_list|(
name|int
name|numberOfShards
parameter_list|,
name|long
name|numberOfDocs
parameter_list|,
name|int
name|iteration
parameter_list|,
name|SearchResponse
name|searchResponse
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"iteration [{}] - successful shards: {} (expected {})"
argument_list|,
name|iteration
argument_list|,
name|searchResponse
operator|.
name|getSuccessfulShards
argument_list|()
argument_list|,
name|numberOfShards
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"iteration [{}] - failed shards: {} (expected 0)"
argument_list|,
name|iteration
argument_list|,
name|searchResponse
operator|.
name|getFailedShards
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|searchResponse
operator|.
name|getShardFailures
argument_list|()
operator|!=
literal|null
operator|&&
name|searchResponse
operator|.
name|getShardFailures
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"iteration [{}] - shard failures: {}"
argument_list|,
name|iteration
argument_list|,
name|Arrays
operator|.
name|toString
argument_list|(
name|searchResponse
operator|.
name|getShardFailures
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"iteration [{}] - returned documents: {} (expected {})"
argument_list|,
name|iteration
argument_list|,
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|numberOfDocs
argument_list|)
expr_stmt|;
block|}
DECL|method|refreshAndAssert
specifier|private
name|void
name|refreshAndAssert
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|assertThat
argument_list|(
name|awaitBusy
argument_list|(
operator|new
name|Predicate
argument_list|<
name|Object
argument_list|>
argument_list|()
block|{
specifier|public
name|boolean
name|apply
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
try|try
block|{
name|RefreshResponse
name|actionGet
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
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertNoFailures
argument_list|(
name|actionGet
argument_list|)
expr_stmt|;
return|return
name|actionGet
operator|.
name|getTotalShards
argument_list|()
operator|==
name|actionGet
operator|.
name|getSuccessfulShards
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
argument_list|,
literal|5
argument_list|,
name|TimeUnit
operator|.
name|MINUTES
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

