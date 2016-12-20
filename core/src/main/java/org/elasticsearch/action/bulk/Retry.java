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
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

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
name|PlainActionFuture
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
name|util
operator|.
name|concurrent
operator|.
name|FutureUtils
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
name|Iterator
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
name|concurrent
operator|.
name|ScheduledFuture
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
name|Predicate
import|;
end_import

begin_comment
comment|/**  * Encapsulates synchronous and asynchronous retry logic.  */
end_comment

begin_class
DECL|class|Retry
specifier|public
class|class
name|Retry
block|{
DECL|field|retryOnThrowable
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Throwable
argument_list|>
name|retryOnThrowable
decl_stmt|;
DECL|field|backoffPolicy
specifier|private
name|BackoffPolicy
name|backoffPolicy
decl_stmt|;
DECL|method|on
specifier|public
specifier|static
name|Retry
name|on
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Throwable
argument_list|>
name|retryOnThrowable
parameter_list|)
block|{
return|return
operator|new
name|Retry
argument_list|(
name|retryOnThrowable
argument_list|)
return|;
block|}
comment|/**      * @param backoffPolicy The backoff policy that defines how long and how often to wait for retries.      */
DECL|method|policy
specifier|public
name|Retry
name|policy
parameter_list|(
name|BackoffPolicy
name|backoffPolicy
parameter_list|)
block|{
name|this
operator|.
name|backoffPolicy
operator|=
name|backoffPolicy
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|Retry
name|Retry
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Throwable
argument_list|>
name|retryOnThrowable
parameter_list|)
block|{
name|this
operator|.
name|retryOnThrowable
operator|=
name|retryOnThrowable
expr_stmt|;
block|}
comment|/**      * Invokes #bulk(BulkRequest, ActionListener) on the provided client. Backs off on the provided exception and delegates results to the      * provided listener.      *      * @param client      Client invoking the bulk request.      * @param bulkRequest The bulk request that should be executed.      * @param listener    A listener that is invoked when the bulk request finishes or completes with an exception. The listener is not      */
DECL|method|withAsyncBackoff
specifier|public
name|void
name|withAsyncBackoff
parameter_list|(
name|Client
name|client
parameter_list|,
name|BulkRequest
name|bulkRequest
parameter_list|,
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|AsyncRetryHandler
name|r
init|=
operator|new
name|AsyncRetryHandler
argument_list|(
name|retryOnThrowable
argument_list|,
name|backoffPolicy
argument_list|,
name|client
argument_list|,
name|listener
argument_list|)
decl_stmt|;
name|r
operator|.
name|execute
argument_list|(
name|bulkRequest
argument_list|)
expr_stmt|;
block|}
comment|/**      * Invokes #bulk(BulkRequest) on the provided client. Backs off on the provided exception.      *      * @param client      Client invoking the bulk request.      * @param bulkRequest The bulk request that should be executed.      * @return the bulk response as returned by the client.      * @throws Exception Any exception thrown by the callable.      */
DECL|method|withSyncBackoff
specifier|public
name|BulkResponse
name|withSyncBackoff
parameter_list|(
name|Client
name|client
parameter_list|,
name|BulkRequest
name|bulkRequest
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|SyncRetryHandler
operator|.
name|create
argument_list|(
name|retryOnThrowable
argument_list|,
name|backoffPolicy
argument_list|,
name|client
argument_list|)
operator|.
name|executeBlocking
argument_list|(
name|bulkRequest
argument_list|)
operator|.
name|actionGet
argument_list|()
return|;
block|}
DECL|class|AbstractRetryHandler
specifier|static
class|class
name|AbstractRetryHandler
implements|implements
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
block|{
DECL|field|logger
specifier|private
specifier|final
name|Logger
name|logger
decl_stmt|;
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|listener
specifier|private
specifier|final
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|listener
decl_stmt|;
DECL|field|backoff
specifier|private
specifier|final
name|Iterator
argument_list|<
name|TimeValue
argument_list|>
name|backoff
decl_stmt|;
DECL|field|retryOnThrowable
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|Throwable
argument_list|>
name|retryOnThrowable
decl_stmt|;
comment|// Access only when holding a client-side lock, see also #addResponses()
DECL|field|responses
specifier|private
specifier|final
name|List
argument_list|<
name|BulkItemResponse
argument_list|>
name|responses
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|startTimestampNanos
specifier|private
specifier|final
name|long
name|startTimestampNanos
decl_stmt|;
comment|// needed to construct the next bulk request based on the response to the previous one
comment|// volatile as we're called from a scheduled thread
DECL|field|currentBulkRequest
specifier|private
specifier|volatile
name|BulkRequest
name|currentBulkRequest
decl_stmt|;
DECL|field|scheduledRequestFuture
specifier|private
specifier|volatile
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|scheduledRequestFuture
decl_stmt|;
DECL|method|AbstractRetryHandler
specifier|public
name|AbstractRetryHandler
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Throwable
argument_list|>
name|retryOnThrowable
parameter_list|,
name|BackoffPolicy
name|backoffPolicy
parameter_list|,
name|Client
name|client
parameter_list|,
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|this
operator|.
name|retryOnThrowable
operator|=
name|retryOnThrowable
expr_stmt|;
name|this
operator|.
name|backoff
operator|=
name|backoffPolicy
operator|.
name|iterator
argument_list|()
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|getClass
argument_list|()
argument_list|,
name|client
operator|.
name|settings
argument_list|()
argument_list|)
expr_stmt|;
comment|// in contrast to System.currentTimeMillis(), nanoTime() uses a monotonic clock under the hood
name|this
operator|.
name|startTimestampNanos
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onResponse
specifier|public
name|void
name|onResponse
parameter_list|(
name|BulkResponse
name|bulkItemResponses
parameter_list|)
block|{
if|if
condition|(
operator|!
name|bulkItemResponses
operator|.
name|hasFailures
argument_list|()
condition|)
block|{
comment|// we're done here, include all responses
name|addResponses
argument_list|(
name|bulkItemResponses
argument_list|,
operator|(
name|r
lambda|->
literal|true
operator|)
argument_list|)
expr_stmt|;
name|finishHim
argument_list|()
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|canRetry
argument_list|(
name|bulkItemResponses
argument_list|)
condition|)
block|{
name|addResponses
argument_list|(
name|bulkItemResponses
argument_list|,
operator|(
name|r
lambda|->
operator|!
name|r
operator|.
name|isFailed
argument_list|()
operator|)
argument_list|)
expr_stmt|;
name|retry
argument_list|(
name|createBulkRequestForRetry
argument_list|(
name|bulkItemResponses
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|addResponses
argument_list|(
name|bulkItemResponses
argument_list|,
operator|(
name|r
lambda|->
literal|true
operator|)
argument_list|)
expr_stmt|;
name|finishHim
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
try|try
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|FutureUtils
operator|.
name|cancel
argument_list|(
name|scheduledRequestFuture
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|retry
specifier|private
name|void
name|retry
parameter_list|(
name|BulkRequest
name|bulkRequestForRetry
parameter_list|)
block|{
assert|assert
name|backoff
operator|.
name|hasNext
argument_list|()
assert|;
name|TimeValue
name|next
init|=
name|backoff
operator|.
name|next
argument_list|()
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"Retry of bulk request scheduled in {} ms."
argument_list|,
name|next
operator|.
name|millis
argument_list|()
argument_list|)
expr_stmt|;
name|Runnable
name|retry
init|=
parameter_list|()
lambda|->
name|this
operator|.
name|execute
argument_list|(
name|bulkRequestForRetry
argument_list|)
decl_stmt|;
name|retry
operator|=
name|client
operator|.
name|threadPool
argument_list|()
operator|.
name|getThreadContext
argument_list|()
operator|.
name|preserveContext
argument_list|(
name|retry
argument_list|)
expr_stmt|;
name|scheduledRequestFuture
operator|=
name|client
operator|.
name|threadPool
argument_list|()
operator|.
name|schedule
argument_list|(
name|next
argument_list|,
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
argument_list|,
name|retry
argument_list|)
expr_stmt|;
block|}
DECL|method|createBulkRequestForRetry
specifier|private
name|BulkRequest
name|createBulkRequestForRetry
parameter_list|(
name|BulkResponse
name|bulkItemResponses
parameter_list|)
block|{
name|BulkRequest
name|requestToReissue
init|=
operator|new
name|BulkRequest
argument_list|()
decl_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|BulkItemResponse
name|bulkItemResponse
range|:
name|bulkItemResponses
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
name|requestToReissue
operator|.
name|add
argument_list|(
name|currentBulkRequest
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
name|index
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|index
operator|++
expr_stmt|;
block|}
return|return
name|requestToReissue
return|;
block|}
DECL|method|canRetry
specifier|private
name|boolean
name|canRetry
parameter_list|(
name|BulkResponse
name|bulkItemResponses
parameter_list|)
block|{
if|if
condition|(
operator|!
name|backoff
operator|.
name|hasNext
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
for|for
control|(
name|BulkItemResponse
name|bulkItemResponse
range|:
name|bulkItemResponses
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
specifier|final
name|Throwable
name|cause
init|=
name|bulkItemResponse
operator|.
name|getFailure
argument_list|()
operator|.
name|getCause
argument_list|()
decl_stmt|;
specifier|final
name|Throwable
name|rootCause
init|=
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|cause
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|rootCause
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|retryOnThrowable
argument_list|)
condition|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
return|return
literal|true
return|;
block|}
DECL|method|finishHim
specifier|private
name|void
name|finishHim
parameter_list|()
block|{
try|try
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|getAccumulatedResponse
argument_list|()
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|FutureUtils
operator|.
name|cancel
argument_list|(
name|scheduledRequestFuture
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|addResponses
specifier|private
name|void
name|addResponses
parameter_list|(
name|BulkResponse
name|response
parameter_list|,
name|Predicate
argument_list|<
name|BulkItemResponse
argument_list|>
name|filter
parameter_list|)
block|{
for|for
control|(
name|BulkItemResponse
name|bulkItemResponse
range|:
name|response
control|)
block|{
if|if
condition|(
name|filter
operator|.
name|test
argument_list|(
name|bulkItemResponse
argument_list|)
condition|)
block|{
comment|// Use client-side lock here to avoid visibility issues. This method may be called multiple times
comment|// (based on how many retries we have to issue) and relying that the response handling code will be
comment|// scheduled on the same thread is fragile.
synchronized|synchronized
init|(
name|responses
init|)
block|{
name|responses
operator|.
name|add
argument_list|(
name|bulkItemResponse
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|getAccumulatedResponse
specifier|private
name|BulkResponse
name|getAccumulatedResponse
parameter_list|()
block|{
name|BulkItemResponse
index|[]
name|itemResponses
decl_stmt|;
synchronized|synchronized
init|(
name|responses
init|)
block|{
name|itemResponses
operator|=
name|responses
operator|.
name|toArray
argument_list|(
operator|new
name|BulkItemResponse
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
name|long
name|stopTimestamp
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|long
name|totalLatencyMs
init|=
name|TimeValue
operator|.
name|timeValueNanos
argument_list|(
name|stopTimestamp
operator|-
name|startTimestampNanos
argument_list|)
operator|.
name|millis
argument_list|()
decl_stmt|;
return|return
operator|new
name|BulkResponse
argument_list|(
name|itemResponses
argument_list|,
name|totalLatencyMs
argument_list|)
return|;
block|}
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|BulkRequest
name|bulkRequest
parameter_list|)
block|{
name|this
operator|.
name|currentBulkRequest
operator|=
name|bulkRequest
expr_stmt|;
name|client
operator|.
name|bulk
argument_list|(
name|bulkRequest
argument_list|,
name|this
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|AsyncRetryHandler
specifier|static
class|class
name|AsyncRetryHandler
extends|extends
name|AbstractRetryHandler
block|{
DECL|method|AsyncRetryHandler
specifier|public
name|AsyncRetryHandler
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Throwable
argument_list|>
name|retryOnThrowable
parameter_list|,
name|BackoffPolicy
name|backoffPolicy
parameter_list|,
name|Client
name|client
parameter_list|,
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|super
argument_list|(
name|retryOnThrowable
argument_list|,
name|backoffPolicy
argument_list|,
name|client
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|SyncRetryHandler
specifier|static
class|class
name|SyncRetryHandler
extends|extends
name|AbstractRetryHandler
block|{
DECL|field|actionFuture
specifier|private
specifier|final
name|PlainActionFuture
argument_list|<
name|BulkResponse
argument_list|>
name|actionFuture
decl_stmt|;
DECL|method|create
specifier|public
specifier|static
name|SyncRetryHandler
name|create
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Throwable
argument_list|>
name|retryOnThrowable
parameter_list|,
name|BackoffPolicy
name|backoffPolicy
parameter_list|,
name|Client
name|client
parameter_list|)
block|{
name|PlainActionFuture
argument_list|<
name|BulkResponse
argument_list|>
name|actionFuture
init|=
name|PlainActionFuture
operator|.
name|newFuture
argument_list|()
decl_stmt|;
return|return
operator|new
name|SyncRetryHandler
argument_list|(
name|retryOnThrowable
argument_list|,
name|backoffPolicy
argument_list|,
name|client
argument_list|,
name|actionFuture
argument_list|)
return|;
block|}
DECL|method|SyncRetryHandler
specifier|public
name|SyncRetryHandler
parameter_list|(
name|Class
argument_list|<
name|?
extends|extends
name|Throwable
argument_list|>
name|retryOnThrowable
parameter_list|,
name|BackoffPolicy
name|backoffPolicy
parameter_list|,
name|Client
name|client
parameter_list|,
name|PlainActionFuture
argument_list|<
name|BulkResponse
argument_list|>
name|actionFuture
parameter_list|)
block|{
name|super
argument_list|(
name|retryOnThrowable
argument_list|,
name|backoffPolicy
argument_list|,
name|client
argument_list|,
name|actionFuture
argument_list|)
expr_stmt|;
name|this
operator|.
name|actionFuture
operator|=
name|actionFuture
expr_stmt|;
block|}
DECL|method|executeBlocking
specifier|public
name|ActionFuture
argument_list|<
name|BulkResponse
argument_list|>
name|executeBlocking
parameter_list|(
name|BulkRequest
name|bulkRequest
parameter_list|)
block|{
name|super
operator|.
name|execute
argument_list|(
name|bulkRequest
argument_list|)
expr_stmt|;
return|return
name|actionFuture
return|;
block|}
block|}
block|}
end_class

end_unit

