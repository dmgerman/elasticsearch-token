begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.bulk
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|bulk
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|RefreshRequest
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
name|EsRejectedExecutionException
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
name|ESIntegTestCase
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
name|ConcurrentHashMap
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
name|TimeUnit
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
name|lessThanOrEqualTo
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
argument_list|,
name|numDataNodes
operator|=
literal|2
argument_list|)
DECL|class|BulkProcessorRetryIT
specifier|public
class|class
name|BulkProcessorRetryIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|INDEX_NAME
specifier|private
specifier|static
specifier|final
name|String
name|INDEX_NAME
init|=
literal|"test"
decl_stmt|;
DECL|field|TYPE_NAME
specifier|private
specifier|static
specifier|final
name|String
name|TYPE_NAME
init|=
literal|"type"
decl_stmt|;
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
comment|//Have very low pool and queue sizes to overwhelm internal pools easily
return|return
name|Settings
operator|.
name|builder
argument_list|()
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
name|put
argument_list|(
literal|"threadpool.generic.size"
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"threadpool.generic.queue_size"
argument_list|,
literal|1
argument_list|)
comment|// don't mess with this one! It's quite sensitive to a low queue size
comment|// (see also ThreadedActionListener which is happily spawning threads even when we already got rejected)
comment|//.put("threadpool.listener.queue_size", 1)
operator|.
name|put
argument_list|(
literal|"threadpool.get.queue_size"
argument_list|,
literal|1
argument_list|)
comment|// default is 50
operator|.
name|put
argument_list|(
literal|"threadpool.bulk.queue_size"
argument_list|,
literal|20
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
DECL|method|testBulkRejectionLoadWithoutBackoff
specifier|public
name|void
name|testBulkRejectionLoadWithoutBackoff
parameter_list|()
throws|throws
name|Throwable
block|{
name|boolean
name|rejectedExecutionExpected
init|=
literal|true
decl_stmt|;
name|executeBulkRejectionLoad
argument_list|(
name|BackoffPolicy
operator|.
name|noBackoff
argument_list|()
argument_list|,
name|rejectedExecutionExpected
argument_list|)
expr_stmt|;
block|}
DECL|method|testBulkRejectionLoadWithBackoff
specifier|public
name|void
name|testBulkRejectionLoadWithBackoff
parameter_list|()
throws|throws
name|Throwable
block|{
name|boolean
name|rejectedExecutionExpected
init|=
literal|false
decl_stmt|;
name|executeBulkRejectionLoad
argument_list|(
name|BackoffPolicy
operator|.
name|exponentialBackoff
argument_list|()
argument_list|,
name|rejectedExecutionExpected
argument_list|)
expr_stmt|;
block|}
DECL|method|executeBulkRejectionLoad
specifier|private
name|void
name|executeBulkRejectionLoad
parameter_list|(
name|BackoffPolicy
name|backoffPolicy
parameter_list|,
name|boolean
name|rejectedExecutionExpected
parameter_list|)
throws|throws
name|Throwable
block|{
name|int
name|numberOfAsyncOps
init|=
name|randomIntBetween
argument_list|(
literal|600
argument_list|,
literal|700
argument_list|)
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
name|numberOfAsyncOps
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|Object
argument_list|>
name|responses
init|=
name|Collections
operator|.
name|newSetFromMap
argument_list|(
operator|new
name|ConcurrentHashMap
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
name|INDEX_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|BulkProcessor
name|bulkProcessor
init|=
name|BulkProcessor
operator|.
name|builder
argument_list|(
name|client
argument_list|()
argument_list|,
operator|new
name|BulkProcessor
operator|.
name|Listener
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|beforeBulk
parameter_list|(
name|long
name|executionId
parameter_list|,
name|BulkRequest
name|request
parameter_list|)
block|{
comment|// no op
block|}
annotation|@
name|Override
specifier|public
name|void
name|afterBulk
parameter_list|(
name|long
name|executionId
parameter_list|,
name|BulkRequest
name|request
parameter_list|,
name|BulkResponse
name|response
parameter_list|)
block|{
name|responses
operator|.
name|add
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|afterBulk
parameter_list|(
name|long
name|executionId
parameter_list|,
name|BulkRequest
name|request
parameter_list|,
name|Throwable
name|failure
parameter_list|)
block|{
name|responses
operator|.
name|add
argument_list|(
name|failure
argument_list|)
expr_stmt|;
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|setBulkActions
argument_list|(
literal|1
argument_list|)
comment|// zero means that we're in the sync case, more means that we're in the async case
operator|.
name|setConcurrentRequests
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|100
argument_list|)
argument_list|)
operator|.
name|setBackoffPolicy
argument_list|(
name|backoffPolicy
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|indexDocs
argument_list|(
name|bulkProcessor
argument_list|,
name|numberOfAsyncOps
argument_list|)
expr_stmt|;
name|latch
operator|.
name|await
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|bulkProcessor
operator|.
name|close
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|responses
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numberOfAsyncOps
argument_list|)
argument_list|)
expr_stmt|;
comment|// validate all responses
for|for
control|(
name|Object
name|response
range|:
name|responses
control|)
block|{
if|if
condition|(
name|response
operator|instanceof
name|BulkResponse
condition|)
block|{
name|BulkResponse
name|bulkResponse
init|=
operator|(
name|BulkResponse
operator|)
name|response
decl_stmt|;
for|for
control|(
name|BulkItemResponse
name|bulkItemResponse
range|:
name|bulkResponse
operator|.
name|getItems
argument_list|()
control|)
block|{
if|if
condition|(
name|bulkItemResponse
operator|.
name|isFailed
argument_list|()
condition|)
block|{
name|BulkItemResponse
operator|.
name|Failure
name|failure
init|=
name|bulkItemResponse
operator|.
name|getFailure
argument_list|()
decl_stmt|;
name|Throwable
name|rootCause
init|=
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|failure
operator|.
name|getCause
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|rootCause
operator|instanceof
name|EsRejectedExecutionException
condition|)
block|{
if|if
condition|(
name|rejectedExecutionExpected
operator|==
literal|false
condition|)
block|{
comment|// we're not expecting that we overwhelmed it even once
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unexpected failure reason"
argument_list|,
name|rootCause
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unexpected failure"
argument_list|,
name|rootCause
argument_list|)
throw|;
block|}
block|}
block|}
block|}
else|else
block|{
name|Throwable
name|t
init|=
operator|(
name|Throwable
operator|)
name|response
decl_stmt|;
comment|// we're not expecting any other errors
throw|throw
operator|new
name|AssertionError
argument_list|(
literal|"Unexpected failure"
argument_list|,
name|t
argument_list|)
throw|;
block|}
block|}
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
operator|new
name|RefreshRequest
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|// validate we did not create any duplicates due to retries
name|Matcher
argument_list|<
name|Long
argument_list|>
name|searchResultCount
decl_stmt|;
if|if
condition|(
name|rejectedExecutionExpected
condition|)
block|{
comment|// it is ok if we lost some index operations to rejected executions
name|searchResultCount
operator|=
name|lessThanOrEqualTo
argument_list|(
operator|(
name|long
operator|)
name|numberOfAsyncOps
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|searchResultCount
operator|=
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numberOfAsyncOps
argument_list|)
expr_stmt|;
block|}
name|SearchResponse
name|results
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|INDEX_NAME
argument_list|)
operator|.
name|setTypes
argument_list|(
name|TYPE_NAME
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setSize
argument_list|(
literal|0
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|results
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|searchResultCount
argument_list|)
expr_stmt|;
block|}
DECL|method|indexDocs
specifier|private
specifier|static
name|void
name|indexDocs
parameter_list|(
name|BulkProcessor
name|processor
parameter_list|,
name|int
name|numDocs
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|processor
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
name|INDEX_NAME
argument_list|)
operator|.
name|setType
argument_list|(
name|TYPE_NAME
argument_list|)
operator|.
name|setId
argument_list|(
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
name|randomRealisticUnicodeOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|30
argument_list|)
argument_list|)
operator|.
name|request
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

