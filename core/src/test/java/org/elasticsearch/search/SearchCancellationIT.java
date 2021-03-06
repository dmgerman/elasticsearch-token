begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search
package|package
name|org
operator|.
name|elasticsearch
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
name|ActionFuture
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
name|tasks
operator|.
name|cancel
operator|.
name|CancelTasksResponse
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
name|tasks
operator|.
name|list
operator|.
name|ListTasksResponse
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
name|BulkRequestBuilder
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
name|SearchAction
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
name|SearchPhaseExecutionException
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
name|SearchScrollAction
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
name|unit
operator|.
name|TimeValue
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
name|XContentHelper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|PluginsService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|MockScriptPlugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Script
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptType
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
name|lookup
operator|.
name|LeafFieldsLookup
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
name|TaskInfo
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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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
name|List
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
name|AtomicInteger
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Function
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
name|scriptQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchCancellationIT
operator|.
name|ScriptedBlockPlugin
operator|.
name|SCRIPT_NAME
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
name|greaterThan
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
name|hasSize
import|;
end_import

begin_class
annotation|@
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|SUITE
argument_list|)
DECL|class|SearchCancellationIT
specifier|public
class|class
name|SearchCancellationIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|FORMAT_PARAMS
specifier|private
specifier|static
specifier|final
name|ToXContent
operator|.
name|Params
name|FORMAT_PARAMS
init|=
operator|new
name|ToXContent
operator|.
name|MapParams
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"pretty"
argument_list|,
literal|"false"
argument_list|)
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singleton
argument_list|(
name|ScriptedBlockPlugin
operator|.
name|class
argument_list|)
return|;
block|}
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
name|boolean
name|lowLevelCancellation
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Using lowLevelCancellation: {}"
argument_list|,
name|lowLevelCancellation
argument_list|)
expr_stmt|;
return|return
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|SearchService
operator|.
name|LOW_LEVEL_CANCELLATION_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|lowLevelCancellation
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|indexTestData
specifier|private
name|void
name|indexTestData
parameter_list|()
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
literal|5
condition|;
name|i
operator|++
control|)
block|{
comment|// Make sure we have a few segments
name|BulkRequestBuilder
name|bulkRequestBuilder
init|=
name|client
argument_list|()
operator|.
name|prepareBulk
argument_list|()
operator|.
name|setRefreshPolicy
argument_list|(
name|WriteRequest
operator|.
name|RefreshPolicy
operator|.
name|IMMEDIATE
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
literal|20
condition|;
name|j
operator|++
control|)
block|{
name|bulkRequestBuilder
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
operator|*
literal|5
operator|+
name|j
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertNoFailures
argument_list|(
name|bulkRequestBuilder
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|initBlockFactory
specifier|private
name|List
argument_list|<
name|ScriptedBlockPlugin
argument_list|>
name|initBlockFactory
parameter_list|()
block|{
name|List
argument_list|<
name|ScriptedBlockPlugin
argument_list|>
name|plugins
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|PluginsService
name|pluginsService
range|:
name|internalCluster
argument_list|()
operator|.
name|getDataNodeInstances
argument_list|(
name|PluginsService
operator|.
name|class
argument_list|)
control|)
block|{
name|plugins
operator|.
name|addAll
argument_list|(
name|pluginsService
operator|.
name|filterPlugins
argument_list|(
name|ScriptedBlockPlugin
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|ScriptedBlockPlugin
name|plugin
range|:
name|plugins
control|)
block|{
name|plugin
operator|.
name|reset
argument_list|()
expr_stmt|;
name|plugin
operator|.
name|enableBlock
argument_list|()
expr_stmt|;
block|}
return|return
name|plugins
return|;
block|}
DECL|method|awaitForBlock
specifier|private
name|void
name|awaitForBlock
parameter_list|(
name|List
argument_list|<
name|ScriptedBlockPlugin
argument_list|>
name|plugins
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|numberOfShards
init|=
name|getNumShards
argument_list|(
literal|"test"
argument_list|)
operator|.
name|numPrimaries
decl_stmt|;
name|assertBusy
argument_list|(
parameter_list|()
lambda|->
block|{
name|int
name|numberOfBlockedPlugins
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ScriptedBlockPlugin
name|plugin
range|:
name|plugins
control|)
block|{
name|numberOfBlockedPlugins
operator|+=
name|plugin
operator|.
name|hits
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"The plugin blocked on {} out of {} shards"
argument_list|,
name|numberOfBlockedPlugins
argument_list|,
name|numberOfShards
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|numberOfBlockedPlugins
argument_list|,
name|greaterThan
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|disableBlocks
specifier|private
name|void
name|disableBlocks
parameter_list|(
name|List
argument_list|<
name|ScriptedBlockPlugin
argument_list|>
name|plugins
parameter_list|)
throws|throws
name|Exception
block|{
for|for
control|(
name|ScriptedBlockPlugin
name|plugin
range|:
name|plugins
control|)
block|{
name|plugin
operator|.
name|disableBlock
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|cancelSearch
specifier|private
name|void
name|cancelSearch
parameter_list|(
name|String
name|action
parameter_list|)
block|{
name|ListTasksResponse
name|listTasksResponse
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
name|prepareListTasks
argument_list|()
operator|.
name|setActions
argument_list|(
name|action
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|listTasksResponse
operator|.
name|getTasks
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|TaskInfo
name|searchTask
init|=
name|listTasksResponse
operator|.
name|getTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Cancelling search"
argument_list|)
expr_stmt|;
name|CancelTasksResponse
name|cancelTasksResponse
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
name|prepareCancelTasks
argument_list|()
operator|.
name|setTaskId
argument_list|(
name|searchTask
operator|.
name|getTaskId
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|cancelTasksResponse
operator|.
name|getTasks
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cancelTasksResponse
operator|.
name|getTasks
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTaskId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|searchTask
operator|.
name|getTaskId
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|ensureSearchWasCancelled
specifier|private
name|SearchResponse
name|ensureSearchWasCancelled
parameter_list|(
name|ActionFuture
argument_list|<
name|SearchResponse
argument_list|>
name|searchResponse
parameter_list|)
block|{
try|try
block|{
name|SearchResponse
name|response
init|=
name|searchResponse
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Search response {}"
argument_list|,
name|response
argument_list|)
expr_stmt|;
name|assertNotEquals
argument_list|(
literal|"At least one shard should have failed"
argument_list|,
literal|0
argument_list|,
name|response
operator|.
name|getFailedShards
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|response
return|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|info
argument_list|(
literal|"All shards failed with"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
DECL|method|testCancellationDuringQueryPhase
specifier|public
name|void
name|testCancellationDuringQueryPhase
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|ScriptedBlockPlugin
argument_list|>
name|plugins
init|=
name|initBlockFactory
argument_list|()
decl_stmt|;
name|indexTestData
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Executing search"
argument_list|)
expr_stmt|;
name|ActionFuture
argument_list|<
name|SearchResponse
argument_list|>
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|scriptQuery
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"mockscript"
argument_list|,
name|SCRIPT_NAME
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
decl_stmt|;
name|awaitForBlock
argument_list|(
name|plugins
argument_list|)
expr_stmt|;
name|cancelSearch
argument_list|(
name|SearchAction
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|disableBlocks
argument_list|(
name|plugins
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Segments {}"
argument_list|,
name|XContentHelper
operator|.
name|toString
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
name|prepareSegments
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|FORMAT_PARAMS
argument_list|)
argument_list|)
expr_stmt|;
name|ensureSearchWasCancelled
argument_list|(
name|searchResponse
argument_list|)
expr_stmt|;
block|}
DECL|method|testCancellationDuringFetchPhase
specifier|public
name|void
name|testCancellationDuringFetchPhase
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|ScriptedBlockPlugin
argument_list|>
name|plugins
init|=
name|initBlockFactory
argument_list|()
decl_stmt|;
name|indexTestData
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Executing search"
argument_list|)
expr_stmt|;
name|ActionFuture
argument_list|<
name|SearchResponse
argument_list|>
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"test_field"
argument_list|,
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"mockscript"
argument_list|,
name|SCRIPT_NAME
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
decl_stmt|;
name|awaitForBlock
argument_list|(
name|plugins
argument_list|)
expr_stmt|;
name|cancelSearch
argument_list|(
name|SearchAction
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|disableBlocks
argument_list|(
name|plugins
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Segments {}"
argument_list|,
name|XContentHelper
operator|.
name|toString
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
name|prepareSegments
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|FORMAT_PARAMS
argument_list|)
argument_list|)
expr_stmt|;
name|ensureSearchWasCancelled
argument_list|(
name|searchResponse
argument_list|)
expr_stmt|;
block|}
DECL|method|testCancellationOfScrollSearches
specifier|public
name|void
name|testCancellationOfScrollSearches
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|ScriptedBlockPlugin
argument_list|>
name|plugins
init|=
name|initBlockFactory
argument_list|()
decl_stmt|;
name|indexTestData
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Executing search"
argument_list|)
expr_stmt|;
name|ActionFuture
argument_list|<
name|SearchResponse
argument_list|>
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setScroll
argument_list|(
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
argument_list|)
operator|.
name|setSize
argument_list|(
literal|5
argument_list|)
operator|.
name|setQuery
argument_list|(
name|scriptQuery
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"mockscript"
argument_list|,
name|SCRIPT_NAME
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
decl_stmt|;
name|awaitForBlock
argument_list|(
name|plugins
argument_list|)
expr_stmt|;
name|cancelSearch
argument_list|(
name|SearchAction
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|disableBlocks
argument_list|(
name|plugins
argument_list|)
expr_stmt|;
name|SearchResponse
name|response
init|=
name|ensureSearchWasCancelled
argument_list|(
name|searchResponse
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|!=
literal|null
condition|)
block|{
comment|// The response might not have failed on all shards - we need to clean scroll
name|logger
operator|.
name|info
argument_list|(
literal|"Cleaning scroll with id {}"
argument_list|,
name|response
operator|.
name|getScrollId
argument_list|()
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareClearScroll
argument_list|()
operator|.
name|addScrollId
argument_list|(
name|response
operator|.
name|getScrollId
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|testCancellationOfScrollSearchesOnFollowupRequests
specifier|public
name|void
name|testCancellationOfScrollSearchesOnFollowupRequests
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|ScriptedBlockPlugin
argument_list|>
name|plugins
init|=
name|initBlockFactory
argument_list|()
decl_stmt|;
name|indexTestData
argument_list|()
expr_stmt|;
comment|// Disable block so the first request would pass
name|disableBlocks
argument_list|(
name|plugins
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Executing search"
argument_list|)
expr_stmt|;
name|TimeValue
name|keepAlive
init|=
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|5
argument_list|)
decl_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setScroll
argument_list|(
name|keepAlive
argument_list|)
operator|.
name|setSize
argument_list|(
literal|2
argument_list|)
operator|.
name|setQuery
argument_list|(
name|scriptQuery
argument_list|(
operator|new
name|Script
argument_list|(
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|"mockscript"
argument_list|,
name|SCRIPT_NAME
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|searchResponse
operator|.
name|getScrollId
argument_list|()
argument_list|)
expr_stmt|;
comment|// Enable block so the second request would block
for|for
control|(
name|ScriptedBlockPlugin
name|plugin
range|:
name|plugins
control|)
block|{
name|plugin
operator|.
name|reset
argument_list|()
expr_stmt|;
name|plugin
operator|.
name|enableBlock
argument_list|()
expr_stmt|;
block|}
name|String
name|scrollId
init|=
name|searchResponse
operator|.
name|getScrollId
argument_list|()
decl_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"Executing scroll with id {}"
argument_list|,
name|scrollId
argument_list|)
expr_stmt|;
name|ActionFuture
argument_list|<
name|SearchResponse
argument_list|>
name|scrollResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearchScroll
argument_list|(
name|searchResponse
operator|.
name|getScrollId
argument_list|()
argument_list|)
operator|.
name|setScroll
argument_list|(
name|keepAlive
argument_list|)
operator|.
name|execute
argument_list|()
decl_stmt|;
name|awaitForBlock
argument_list|(
name|plugins
argument_list|)
expr_stmt|;
name|cancelSearch
argument_list|(
name|SearchScrollAction
operator|.
name|NAME
argument_list|)
expr_stmt|;
name|disableBlocks
argument_list|(
name|plugins
argument_list|)
expr_stmt|;
name|SearchResponse
name|response
init|=
name|ensureSearchWasCancelled
argument_list|(
name|scrollResponse
argument_list|)
decl_stmt|;
if|if
condition|(
name|response
operator|!=
literal|null
condition|)
block|{
comment|// The response didn't fail completely - update scroll id
name|scrollId
operator|=
name|response
operator|.
name|getScrollId
argument_list|()
expr_stmt|;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"Cleaning scroll with id {}"
argument_list|,
name|scrollId
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareClearScroll
argument_list|()
operator|.
name|addScrollId
argument_list|(
name|scrollId
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
DECL|class|ScriptedBlockPlugin
specifier|public
specifier|static
class|class
name|ScriptedBlockPlugin
extends|extends
name|MockScriptPlugin
block|{
DECL|field|SCRIPT_NAME
specifier|static
specifier|final
name|String
name|SCRIPT_NAME
init|=
literal|"search_block"
decl_stmt|;
DECL|field|hits
specifier|private
specifier|final
name|AtomicInteger
name|hits
init|=
operator|new
name|AtomicInteger
argument_list|()
decl_stmt|;
DECL|field|shouldBlock
specifier|private
specifier|final
name|AtomicBoolean
name|shouldBlock
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
DECL|method|reset
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|hits
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
DECL|method|disableBlock
specifier|public
name|void
name|disableBlock
parameter_list|()
block|{
name|shouldBlock
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|enableBlock
specifier|public
name|void
name|enableBlock
parameter_list|()
block|{
name|shouldBlock
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|pluginScripts
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Function
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|,
name|Object
argument_list|>
argument_list|>
name|pluginScripts
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singletonMap
argument_list|(
name|SCRIPT_NAME
argument_list|,
name|params
lambda|->
block|{
name|LeafFieldsLookup
name|fieldsLookup
init|=
operator|(
name|LeafFieldsLookup
operator|)
name|params
operator|.
name|get
argument_list|(
literal|"_fields"
argument_list|)
decl_stmt|;
name|Loggers
operator|.
name|getLogger
argument_list|(
name|SearchCancellationIT
operator|.
name|class
argument_list|)
operator|.
name|info
argument_list|(
literal|"Blocking on the document {}"
argument_list|,
name|fieldsLookup
operator|.
name|get
argument_list|(
literal|"_uid"
argument_list|)
argument_list|)
expr_stmt|;
name|hits
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
try|try
block|{
name|awaitBusy
argument_list|(
parameter_list|()
lambda|->
name|shouldBlock
operator|.
name|get
argument_list|()
operator|==
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
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
return|return
literal|true
return|;
block|}
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

