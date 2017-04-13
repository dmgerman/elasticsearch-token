begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
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
name|ListenableActionFuture
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
name|CancelTasksRequest
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
name|byscroll
operator|.
name|AbstractBulkByScrollRequestBuilder
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
name|byscroll
operator|.
name|BulkByScrollResponse
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
name|byscroll
operator|.
name|BulkByScrollTask
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
name|ingest
operator|.
name|DeletePipelineRequest
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
name|bytes
operator|.
name|BytesArray
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
name|bytes
operator|.
name|BytesReference
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
name|XContentType
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
name|IndexModule
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
name|engine
operator|.
name|Engine
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
name|engine
operator|.
name|Engine
operator|.
name|Operation
operator|.
name|Origin
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
name|index
operator|.
name|shard
operator|.
name|IndexingOperationListener
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
name|ShardId
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|IngestTestPlugin
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
name|hamcrest
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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
name|concurrent
operator|.
name|Semaphore
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
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|IntStream
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
name|assertAcked
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
name|assertHitCount
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
name|emptyIterable
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
name|hasSize
import|;
end_import

begin_comment
comment|/**  * Test that you can actually cancel a reindex/update-by-query/delete-by-query request and all the plumbing works. Doesn't test all of the  * different cancellation places - that is the responsibility of AsyncBulkByScrollActionTests which have more precise control to  * simulate failures but do not exercise important portion of the stack like transport and task management.  */
end_comment

begin_class
annotation|@
name|TestLogging
argument_list|(
literal|"org.elasticsearch.action.bulk.byscroll:DEBUG,org.elasticsearch.index.reindex:DEBUG"
argument_list|)
DECL|class|CancelTests
specifier|public
class|class
name|CancelTests
extends|extends
name|ReindexTestCase
block|{
DECL|field|INDEX
specifier|protected
specifier|static
specifier|final
name|String
name|INDEX
init|=
literal|"reindex-cancel-index"
decl_stmt|;
DECL|field|TYPE
specifier|protected
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"reindex-cancel-type"
decl_stmt|;
comment|// Semaphore used to allow& block indexing operations during the test
DECL|field|ALLOWED_OPERATIONS
specifier|private
specifier|static
specifier|final
name|Semaphore
name|ALLOWED_OPERATIONS
init|=
operator|new
name|Semaphore
argument_list|(
literal|0
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
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|plugins
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|super
operator|.
name|nodePlugins
argument_list|()
argument_list|)
decl_stmt|;
name|plugins
operator|.
name|add
argument_list|(
name|IngestTestPlugin
operator|.
name|class
argument_list|)
expr_stmt|;
name|plugins
operator|.
name|add
argument_list|(
name|ReindexCancellationPlugin
operator|.
name|class
argument_list|)
expr_stmt|;
return|return
name|plugins
return|;
block|}
annotation|@
name|Before
DECL|method|clearAllowedOperations
specifier|public
name|void
name|clearAllowedOperations
parameter_list|()
block|{
name|ALLOWED_OPERATIONS
operator|.
name|drainPermits
argument_list|()
expr_stmt|;
block|}
comment|/**      * Executes the cancellation test      */
DECL|method|testCancel
specifier|private
name|void
name|testCancel
parameter_list|(
name|String
name|action
parameter_list|,
name|AbstractBulkByScrollRequestBuilder
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|builder
parameter_list|,
name|CancelAssertion
name|assertion
parameter_list|,
name|Matcher
argument_list|<
name|String
argument_list|>
name|taskDescriptionMatcher
parameter_list|)
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
name|INDEX
argument_list|)
expr_stmt|;
comment|// Total number of documents created for this test (~10 per primary shard per shard)
name|int
name|numDocs
init|=
name|getNumShards
argument_list|(
name|INDEX
argument_list|)
operator|.
name|numPrimaries
operator|*
literal|10
operator|*
name|builder
operator|.
name|request
argument_list|()
operator|.
name|getSlices
argument_list|()
decl_stmt|;
name|ALLOWED_OPERATIONS
operator|.
name|release
argument_list|(
name|numDocs
argument_list|)
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|,
name|IntStream
operator|.
name|range
argument_list|(
literal|0
argument_list|,
name|numDocs
argument_list|)
operator|.
name|mapToObj
argument_list|(
name|i
lambda|->
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|INDEX
argument_list|,
name|TYPE
argument_list|,
name|String
operator|.
name|valueOf
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"n"
argument_list|,
name|i
argument_list|)
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Checks that the all documents have been indexed and correctly counted
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|INDEX
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|numDocs
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ALLOWED_OPERATIONS
operator|.
name|drainPermits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
comment|// Scroll by 1 so that cancellation is easier to control
name|builder
operator|.
name|source
argument_list|()
operator|.
name|setSize
argument_list|(
literal|1
argument_list|)
expr_stmt|;
comment|/* Allow a random number of the documents less the number of workers to be modified by the reindex action. That way at least one          * worker is blocked. */
name|int
name|numModifiedDocs
init|=
name|randomIntBetween
argument_list|(
name|builder
operator|.
name|request
argument_list|()
operator|.
name|getSlices
argument_list|()
operator|*
literal|2
argument_list|,
name|numDocs
argument_list|)
decl_stmt|;
name|ALLOWED_OPERATIONS
operator|.
name|release
argument_list|(
name|numModifiedDocs
operator|-
name|builder
operator|.
name|request
argument_list|()
operator|.
name|getSlices
argument_list|()
argument_list|)
expr_stmt|;
comment|// Now execute the reindex action...
name|ListenableActionFuture
argument_list|<
name|?
extends|extends
name|BulkByScrollResponse
argument_list|>
name|future
init|=
name|builder
operator|.
name|execute
argument_list|()
decl_stmt|;
comment|/* ... and waits for the indexing operation listeners to block. It is important to realize that some of the workers might have          * exhausted their slice while others might have quite a bit left to work on. We can't control that. */
name|awaitBusy
argument_list|(
parameter_list|()
lambda|->
name|ALLOWED_OPERATIONS
operator|.
name|hasQueuedThreads
argument_list|()
operator|&&
name|ALLOWED_OPERATIONS
operator|.
name|availablePermits
argument_list|()
operator|==
literal|0
argument_list|)
expr_stmt|;
comment|// Status should show the task running
name|TaskInfo
name|mainTask
init|=
name|findTaskToCancel
argument_list|(
name|action
argument_list|,
name|builder
operator|.
name|request
argument_list|()
operator|.
name|getSlices
argument_list|()
argument_list|)
decl_stmt|;
name|BulkByScrollTask
operator|.
name|Status
name|status
init|=
operator|(
name|BulkByScrollTask
operator|.
name|Status
operator|)
name|mainTask
operator|.
name|getStatus
argument_list|()
decl_stmt|;
name|assertNull
argument_list|(
name|status
operator|.
name|getReasonCancelled
argument_list|()
argument_list|)
expr_stmt|;
comment|// Description shouldn't be empty
name|assertThat
argument_list|(
name|mainTask
operator|.
name|getDescription
argument_list|()
argument_list|,
name|taskDescriptionMatcher
argument_list|)
expr_stmt|;
comment|// Cancel the request while the reindex action is blocked by the indexing operation listeners.
comment|// This will prevent further requests from being sent.
name|ListTasksResponse
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
name|mainTask
operator|.
name|getTaskId
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|cancelTasksResponse
operator|.
name|rethrowFailures
argument_list|(
literal|"Cancel"
argument_list|)
expr_stmt|;
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
comment|// The status should now show canceled. The request will still be in the list because it is (or its children are) still blocked.
name|mainTask
operator|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareGetTask
argument_list|(
name|mainTask
operator|.
name|getTaskId
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|getTask
argument_list|()
operator|.
name|getTask
argument_list|()
expr_stmt|;
name|status
operator|=
operator|(
name|BulkByScrollTask
operator|.
name|Status
operator|)
name|mainTask
operator|.
name|getStatus
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
name|CancelTasksRequest
operator|.
name|DEFAULT_REASON
argument_list|,
name|status
operator|.
name|getReasonCancelled
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|builder
operator|.
name|request
argument_list|()
operator|.
name|getSlices
argument_list|()
operator|>
literal|1
condition|)
block|{
name|boolean
name|foundCancelled
init|=
literal|false
decl_stmt|;
name|ListTasksResponse
name|sliceList
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
name|setParentTaskId
argument_list|(
name|mainTask
operator|.
name|getTaskId
argument_list|()
argument_list|)
operator|.
name|setDetailed
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|sliceList
operator|.
name|rethrowFailures
argument_list|(
literal|"Fetch slice tasks"
argument_list|)
expr_stmt|;
for|for
control|(
name|TaskInfo
name|slice
range|:
name|sliceList
operator|.
name|getTasks
argument_list|()
control|)
block|{
name|BulkByScrollTask
operator|.
name|Status
name|sliceStatus
init|=
operator|(
name|BulkByScrollTask
operator|.
name|Status
operator|)
name|slice
operator|.
name|getStatus
argument_list|()
decl_stmt|;
if|if
condition|(
name|sliceStatus
operator|.
name|getReasonCancelled
argument_list|()
operator|==
literal|null
condition|)
continue|continue;
name|assertEquals
argument_list|(
name|CancelTasksRequest
operator|.
name|DEFAULT_REASON
argument_list|,
name|sliceStatus
operator|.
name|getReasonCancelled
argument_list|()
argument_list|)
expr_stmt|;
name|foundCancelled
operator|=
literal|true
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Didn't find at least one sub task that was cancelled"
argument_list|,
name|foundCancelled
argument_list|)
expr_stmt|;
block|}
comment|// Unblock the last operations
name|ALLOWED_OPERATIONS
operator|.
name|release
argument_list|(
name|builder
operator|.
name|request
argument_list|()
operator|.
name|getSlices
argument_list|()
argument_list|)
expr_stmt|;
comment|// Checks that no more operations are executed
name|assertBusy
argument_list|(
parameter_list|()
lambda|->
block|{
if|if
condition|(
name|builder
operator|.
name|request
argument_list|()
operator|.
name|getSlices
argument_list|()
operator|==
literal|1
condition|)
block|{
comment|/* We can only be sure that we've drained all the permits if we only use a single worker. Otherwise some worker may have                  * exhausted all of its documents before we blocked. */
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ALLOWED_OPERATIONS
operator|.
name|availablePermits
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|ALLOWED_OPERATIONS
operator|.
name|getQueueLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
comment|// And check the status of the response
name|BulkByScrollResponse
name|response
decl_stmt|;
try|try
block|{
name|response
operator|=
name|future
operator|.
name|get
argument_list|(
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|String
name|tasks
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
name|setParentTaskId
argument_list|(
name|mainTask
operator|.
name|getTaskId
argument_list|()
argument_list|)
operator|.
name|setDetailed
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Exception while waiting for the response. Running tasks: "
operator|+
name|tasks
argument_list|,
name|e
argument_list|)
throw|;
block|}
name|assertThat
argument_list|(
name|response
operator|.
name|getReasonCancelled
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"by user request"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getBulkFailures
argument_list|()
argument_list|,
name|emptyIterable
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getSearchFailures
argument_list|()
argument_list|,
name|emptyIterable
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|builder
operator|.
name|request
argument_list|()
operator|.
name|getSlices
argument_list|()
operator|>=
literal|1
condition|)
block|{
comment|// If we have more than one worker we might not have made all the modifications
name|numModifiedDocs
operator|-=
name|ALLOWED_OPERATIONS
operator|.
name|availablePermits
argument_list|()
expr_stmt|;
block|}
name|flushAndRefresh
argument_list|(
name|INDEX
argument_list|)
expr_stmt|;
name|assertion
operator|.
name|assertThat
argument_list|(
name|response
argument_list|,
name|numDocs
argument_list|,
name|numModifiedDocs
argument_list|)
expr_stmt|;
block|}
DECL|method|findTaskToCancel
specifier|private
name|TaskInfo
name|findTaskToCancel
parameter_list|(
name|String
name|actionName
parameter_list|,
name|int
name|workerCount
parameter_list|)
block|{
name|ListTasksResponse
name|tasks
decl_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
do|do
block|{
name|tasks
operator|=
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
name|actionName
argument_list|)
operator|.
name|setDetailed
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|tasks
operator|.
name|rethrowFailures
argument_list|(
literal|"Find tasks to cancel"
argument_list|)
expr_stmt|;
for|for
control|(
name|TaskInfo
name|taskInfo
range|:
name|tasks
operator|.
name|getTasks
argument_list|()
control|)
block|{
comment|// Skip tasks with a parent because those are children of the task we want to cancel
if|if
condition|(
literal|false
operator|==
name|taskInfo
operator|.
name|getParentTaskId
argument_list|()
operator|.
name|isSet
argument_list|()
condition|)
block|{
return|return
name|taskInfo
return|;
block|}
block|}
block|}
do|while
condition|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|start
operator|<
name|TimeUnit
operator|.
name|SECONDS
operator|.
name|toNanos
argument_list|(
literal|10
argument_list|)
condition|)
do|;
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Couldn't find task to rethrottle after waiting tasks="
operator|+
name|tasks
operator|.
name|getTasks
argument_list|()
argument_list|)
throw|;
block|}
DECL|method|testReindexCancel
specifier|public
name|void
name|testReindexCancel
parameter_list|()
throws|throws
name|Exception
block|{
name|testCancel
argument_list|(
name|ReindexAction
operator|.
name|NAME
argument_list|,
name|reindex
argument_list|()
operator|.
name|source
argument_list|(
name|INDEX
argument_list|)
operator|.
name|destination
argument_list|(
literal|"dest"
argument_list|,
name|TYPE
argument_list|)
argument_list|,
parameter_list|(
name|response
parameter_list|,
name|total
parameter_list|,
name|modified
parameter_list|)
lambda|->
block|{
name|assertThat
argument_list|(
name|response
argument_list|,
name|matcher
argument_list|()
operator|.
name|created
argument_list|(
name|modified
argument_list|)
operator|.
name|reasonCancelled
argument_list|(
name|equalTo
argument_list|(
literal|"by user request"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|refresh
argument_list|(
literal|"dest"
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"dest"
argument_list|)
operator|.
name|setTypes
argument_list|(
name|TYPE
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|modified
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|equalTo
argument_list|(
literal|"reindex from ["
operator|+
name|INDEX
operator|+
literal|"] to [dest]["
operator|+
name|TYPE
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testUpdateByQueryCancel
specifier|public
name|void
name|testUpdateByQueryCancel
parameter_list|()
throws|throws
name|Exception
block|{
name|BytesReference
name|pipeline
init|=
operator|new
name|BytesArray
argument_list|(
literal|"{\n"
operator|+
literal|"  \"description\" : \"sets processed to true\",\n"
operator|+
literal|"  \"processors\" : [ {\n"
operator|+
literal|"      \"test\" : {}\n"
operator|+
literal|"  } ]\n"
operator|+
literal|"}"
argument_list|)
decl_stmt|;
name|assertAcked
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
name|preparePutPipeline
argument_list|(
literal|"set-processed"
argument_list|,
name|pipeline
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|testCancel
argument_list|(
name|UpdateByQueryAction
operator|.
name|NAME
argument_list|,
name|updateByQuery
argument_list|()
operator|.
name|setPipeline
argument_list|(
literal|"set-processed"
argument_list|)
operator|.
name|source
argument_list|(
name|INDEX
argument_list|)
argument_list|,
parameter_list|(
name|response
parameter_list|,
name|total
parameter_list|,
name|modified
parameter_list|)
lambda|->
block|{
name|assertThat
argument_list|(
name|response
argument_list|,
name|matcher
argument_list|()
operator|.
name|updated
argument_list|(
name|modified
argument_list|)
operator|.
name|reasonCancelled
argument_list|(
name|equalTo
argument_list|(
literal|"by user request"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|INDEX
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
literal|"processed"
argument_list|,
literal|true
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|modified
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|equalTo
argument_list|(
literal|"update-by-query ["
operator|+
name|INDEX
operator|+
literal|"]"
argument_list|)
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
name|cluster
argument_list|()
operator|.
name|deletePipeline
argument_list|(
operator|new
name|DeletePipelineRequest
argument_list|(
literal|"set-processed"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testDeleteByQueryCancel
specifier|public
name|void
name|testDeleteByQueryCancel
parameter_list|()
throws|throws
name|Exception
block|{
name|testCancel
argument_list|(
name|DeleteByQueryAction
operator|.
name|NAME
argument_list|,
name|deleteByQuery
argument_list|()
operator|.
name|source
argument_list|(
name|INDEX
argument_list|)
operator|.
name|filter
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
argument_list|,
parameter_list|(
name|response
parameter_list|,
name|total
parameter_list|,
name|modified
parameter_list|)
lambda|->
block|{
name|assertThat
argument_list|(
name|response
argument_list|,
name|matcher
argument_list|()
operator|.
name|deleted
argument_list|(
name|modified
argument_list|)
operator|.
name|reasonCancelled
argument_list|(
name|equalTo
argument_list|(
literal|"by user request"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|INDEX
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|total
operator|-
name|modified
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|equalTo
argument_list|(
literal|"delete-by-query ["
operator|+
name|INDEX
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testReindexCancelWithWorkers
specifier|public
name|void
name|testReindexCancelWithWorkers
parameter_list|()
throws|throws
name|Exception
block|{
name|testCancel
argument_list|(
name|ReindexAction
operator|.
name|NAME
argument_list|,
name|reindex
argument_list|()
operator|.
name|source
argument_list|(
name|INDEX
argument_list|)
operator|.
name|destination
argument_list|(
literal|"dest"
argument_list|,
name|TYPE
argument_list|)
operator|.
name|setSlices
argument_list|(
literal|5
argument_list|)
argument_list|,
parameter_list|(
name|response
parameter_list|,
name|total
parameter_list|,
name|modified
parameter_list|)
lambda|->
block|{
name|assertThat
argument_list|(
name|response
argument_list|,
name|matcher
argument_list|()
operator|.
name|created
argument_list|(
name|modified
argument_list|)
operator|.
name|reasonCancelled
argument_list|(
name|equalTo
argument_list|(
literal|"by user request"
argument_list|)
argument_list|)
operator|.
name|slices
argument_list|(
name|hasSize
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|refresh
argument_list|(
literal|"dest"
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"dest"
argument_list|)
operator|.
name|setTypes
argument_list|(
name|TYPE
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|modified
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|equalTo
argument_list|(
literal|"reindex from ["
operator|+
name|INDEX
operator|+
literal|"] to [dest]["
operator|+
name|TYPE
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testUpdateByQueryCancelWithWorkers
specifier|public
name|void
name|testUpdateByQueryCancelWithWorkers
parameter_list|()
throws|throws
name|Exception
block|{
name|BytesReference
name|pipeline
init|=
operator|new
name|BytesArray
argument_list|(
literal|"{\n"
operator|+
literal|"  \"description\" : \"sets processed to true\",\n"
operator|+
literal|"  \"processors\" : [ {\n"
operator|+
literal|"      \"test\" : {}\n"
operator|+
literal|"  } ]\n"
operator|+
literal|"}"
argument_list|)
decl_stmt|;
name|assertAcked
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
name|preparePutPipeline
argument_list|(
literal|"set-processed"
argument_list|,
name|pipeline
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|testCancel
argument_list|(
name|UpdateByQueryAction
operator|.
name|NAME
argument_list|,
name|updateByQuery
argument_list|()
operator|.
name|setPipeline
argument_list|(
literal|"set-processed"
argument_list|)
operator|.
name|source
argument_list|(
name|INDEX
argument_list|)
operator|.
name|setSlices
argument_list|(
literal|5
argument_list|)
argument_list|,
parameter_list|(
name|response
parameter_list|,
name|total
parameter_list|,
name|modified
parameter_list|)
lambda|->
block|{
name|assertThat
argument_list|(
name|response
argument_list|,
name|matcher
argument_list|()
operator|.
name|updated
argument_list|(
name|modified
argument_list|)
operator|.
name|reasonCancelled
argument_list|(
name|equalTo
argument_list|(
literal|"by user request"
argument_list|)
argument_list|)
operator|.
name|slices
argument_list|(
name|hasSize
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|INDEX
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
literal|"processed"
argument_list|,
literal|true
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|modified
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|equalTo
argument_list|(
literal|"update-by-query ["
operator|+
name|INDEX
operator|+
literal|"]"
argument_list|)
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
name|cluster
argument_list|()
operator|.
name|deletePipeline
argument_list|(
operator|new
name|DeletePipelineRequest
argument_list|(
literal|"set-processed"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testDeleteByQueryCancelWithWorkers
specifier|public
name|void
name|testDeleteByQueryCancelWithWorkers
parameter_list|()
throws|throws
name|Exception
block|{
name|testCancel
argument_list|(
name|DeleteByQueryAction
operator|.
name|NAME
argument_list|,
name|deleteByQuery
argument_list|()
operator|.
name|source
argument_list|(
name|INDEX
argument_list|)
operator|.
name|filter
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setSlices
argument_list|(
literal|5
argument_list|)
argument_list|,
parameter_list|(
name|response
parameter_list|,
name|total
parameter_list|,
name|modified
parameter_list|)
lambda|->
block|{
name|assertThat
argument_list|(
name|response
argument_list|,
name|matcher
argument_list|()
operator|.
name|deleted
argument_list|(
name|modified
argument_list|)
operator|.
name|reasonCancelled
argument_list|(
name|equalTo
argument_list|(
literal|"by user request"
argument_list|)
argument_list|)
operator|.
name|slices
argument_list|(
name|hasSize
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertHitCount
argument_list|(
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|INDEX
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|total
operator|-
name|modified
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|equalTo
argument_list|(
literal|"delete-by-query ["
operator|+
name|INDEX
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Used to check the result of the cancel test.      */
DECL|interface|CancelAssertion
specifier|private
interface|interface
name|CancelAssertion
block|{
DECL|method|assertThat
name|void
name|assertThat
parameter_list|(
name|BulkByScrollResponse
name|response
parameter_list|,
name|int
name|total
parameter_list|,
name|int
name|modified
parameter_list|)
function_decl|;
block|}
DECL|class|ReindexCancellationPlugin
specifier|public
specifier|static
class|class
name|ReindexCancellationPlugin
extends|extends
name|Plugin
block|{
annotation|@
name|Override
DECL|method|onIndexModule
specifier|public
name|void
name|onIndexModule
parameter_list|(
name|IndexModule
name|indexModule
parameter_list|)
block|{
name|indexModule
operator|.
name|addIndexOperationListener
argument_list|(
operator|new
name|BlockingOperationListener
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|BlockingOperationListener
specifier|public
specifier|static
class|class
name|BlockingOperationListener
implements|implements
name|IndexingOperationListener
block|{
annotation|@
name|Override
DECL|method|preIndex
specifier|public
name|Engine
operator|.
name|Index
name|preIndex
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|Engine
operator|.
name|Index
name|index
parameter_list|)
block|{
return|return
name|preCheck
argument_list|(
name|index
argument_list|,
name|index
operator|.
name|type
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|preDelete
specifier|public
name|Engine
operator|.
name|Delete
name|preDelete
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|Engine
operator|.
name|Delete
name|delete
parameter_list|)
block|{
return|return
name|preCheck
argument_list|(
name|delete
argument_list|,
name|delete
operator|.
name|type
argument_list|()
argument_list|)
return|;
block|}
DECL|method|preCheck
specifier|private
parameter_list|<
name|T
extends|extends
name|Engine
operator|.
name|Operation
parameter_list|>
name|T
name|preCheck
parameter_list|(
name|T
name|operation
parameter_list|,
name|String
name|type
parameter_list|)
block|{
if|if
condition|(
operator|(
name|TYPE
operator|.
name|equals
argument_list|(
name|type
argument_list|)
operator|==
literal|false
operator|)
operator|||
operator|(
name|operation
operator|.
name|origin
argument_list|()
operator|!=
name|Origin
operator|.
name|PRIMARY
operator|)
condition|)
block|{
return|return
name|operation
return|;
block|}
try|try
block|{
if|if
condition|(
name|ALLOWED_OPERATIONS
operator|.
name|tryAcquire
argument_list|(
literal|30
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
return|return
name|operation
return|;
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
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
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Something went wrong"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

