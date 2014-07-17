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
name|ElasticsearchIllegalStateException
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
name|ActionRequest
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
name|DeleteRequest
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
name|IndexRequest
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
name|Nullable
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
name|unit
operator|.
name|ByteSizeUnit
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
name|ByteSizeValue
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
name|EsExecutors
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
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

begin_comment
comment|/**  * A bulk processor is a thread safe bulk processing class, allowing to easily set when to "flush" a new bulk request  * (either based on number of actions, based on the size, or time), and to easily control the number of concurrent bulk  * requests allowed to be executed in parallel.  *<p/>  * In order to create a new bulk processor, use the {@link Builder}.  */
end_comment

begin_class
DECL|class|BulkProcessor
specifier|public
class|class
name|BulkProcessor
implements|implements
name|Closeable
block|{
comment|/**      * A listener for the execution.      */
DECL|interface|Listener
specifier|public
specifier|static
interface|interface
name|Listener
block|{
comment|/**          * Callback before the bulk is executed.          */
DECL|method|beforeBulk
name|void
name|beforeBulk
parameter_list|(
name|long
name|executionId
parameter_list|,
name|BulkRequest
name|request
parameter_list|)
function_decl|;
comment|/**          * Callback after a successful execution of bulk request.          */
DECL|method|afterBulk
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
function_decl|;
comment|/**          * Callback after a failed execution of bulk request.          */
DECL|method|afterBulk
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
function_decl|;
block|}
comment|/**      * A builder used to create a build an instance of a bulk processor.      */
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
block|{
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|listener
specifier|private
specifier|final
name|Listener
name|listener
decl_stmt|;
DECL|field|name
specifier|private
name|String
name|name
decl_stmt|;
DECL|field|concurrentRequests
specifier|private
name|int
name|concurrentRequests
init|=
literal|1
decl_stmt|;
DECL|field|bulkActions
specifier|private
name|int
name|bulkActions
init|=
literal|1000
decl_stmt|;
DECL|field|bulkSize
specifier|private
name|ByteSizeValue
name|bulkSize
init|=
operator|new
name|ByteSizeValue
argument_list|(
literal|5
argument_list|,
name|ByteSizeUnit
operator|.
name|MB
argument_list|)
decl_stmt|;
DECL|field|flushInterval
specifier|private
name|TimeValue
name|flushInterval
init|=
literal|null
decl_stmt|;
comment|/**          * Creates a builder of bulk processor with the client to use and the listener that will be used          * to be notified on the completion of bulk requests.          */
DECL|method|Builder
specifier|public
name|Builder
parameter_list|(
name|Client
name|client
parameter_list|,
name|Listener
name|listener
parameter_list|)
block|{
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
block|}
comment|/**          * Sets an optional name to identify this bulk processor.          */
DECL|method|setName
specifier|public
name|Builder
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Sets the number of concurrent requests allowed to be executed. A value of 0 means that only a single          * request will be allowed to be executed. A value of 1 means 1 concurrent request is allowed to be executed          * while accumulating new bulk requests. Defaults to<tt>1</tt>.          */
DECL|method|setConcurrentRequests
specifier|public
name|Builder
name|setConcurrentRequests
parameter_list|(
name|int
name|concurrentRequests
parameter_list|)
block|{
name|this
operator|.
name|concurrentRequests
operator|=
name|concurrentRequests
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Sets when to flush a new bulk request based on the number of actions currently added. Defaults to          *<tt>1000</tt>. Can be set to<tt>-1</tt> to disable it.          */
DECL|method|setBulkActions
specifier|public
name|Builder
name|setBulkActions
parameter_list|(
name|int
name|bulkActions
parameter_list|)
block|{
name|this
operator|.
name|bulkActions
operator|=
name|bulkActions
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Sets when to flush a new bulk request based on the size of actions currently added. Defaults to          *<tt>5mb</tt>. Can be set to<tt>-1</tt> to disable it.          */
DECL|method|setBulkSize
specifier|public
name|Builder
name|setBulkSize
parameter_list|(
name|ByteSizeValue
name|bulkSize
parameter_list|)
block|{
name|this
operator|.
name|bulkSize
operator|=
name|bulkSize
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Sets a flush interval flushing *any* bulk actions pending if the interval passes. Defaults to not set.          *<p/>          * Note, both {@link #setBulkActions(int)} and {@link #setBulkSize(org.elasticsearch.common.unit.ByteSizeValue)}          * can be set to<tt>-1</tt> with the flush interval set allowing for complete async processing of bulk actions.          */
DECL|method|setFlushInterval
specifier|public
name|Builder
name|setFlushInterval
parameter_list|(
name|TimeValue
name|flushInterval
parameter_list|)
block|{
name|this
operator|.
name|flushInterval
operator|=
name|flushInterval
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Builds a new bulk processor.          */
DECL|method|build
specifier|public
name|BulkProcessor
name|build
parameter_list|()
block|{
return|return
operator|new
name|BulkProcessor
argument_list|(
name|client
argument_list|,
name|listener
argument_list|,
name|name
argument_list|,
name|concurrentRequests
argument_list|,
name|bulkActions
argument_list|,
name|bulkSize
argument_list|,
name|flushInterval
argument_list|)
return|;
block|}
block|}
DECL|method|builder
specifier|public
specifier|static
name|Builder
name|builder
parameter_list|(
name|Client
name|client
parameter_list|,
name|Listener
name|listener
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|(
name|client
argument_list|,
name|listener
argument_list|)
return|;
block|}
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|listener
specifier|private
specifier|final
name|Listener
name|listener
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|concurrentRequests
specifier|private
specifier|final
name|int
name|concurrentRequests
decl_stmt|;
DECL|field|bulkActions
specifier|private
specifier|final
name|int
name|bulkActions
decl_stmt|;
DECL|field|bulkSize
specifier|private
specifier|final
name|long
name|bulkSize
decl_stmt|;
DECL|field|flushInterval
specifier|private
specifier|final
name|TimeValue
name|flushInterval
decl_stmt|;
DECL|field|semaphore
specifier|private
specifier|final
name|Semaphore
name|semaphore
decl_stmt|;
DECL|field|scheduler
specifier|private
specifier|final
name|ScheduledThreadPoolExecutor
name|scheduler
decl_stmt|;
DECL|field|scheduledFuture
specifier|private
specifier|final
name|ScheduledFuture
name|scheduledFuture
decl_stmt|;
DECL|field|executionIdGen
specifier|private
specifier|final
name|AtomicLong
name|executionIdGen
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
DECL|field|bulkRequest
specifier|private
name|BulkRequest
name|bulkRequest
decl_stmt|;
DECL|field|closed
specifier|private
specifier|volatile
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
DECL|method|BulkProcessor
name|BulkProcessor
parameter_list|(
name|Client
name|client
parameter_list|,
name|Listener
name|listener
parameter_list|,
annotation|@
name|Nullable
name|String
name|name
parameter_list|,
name|int
name|concurrentRequests
parameter_list|,
name|int
name|bulkActions
parameter_list|,
name|ByteSizeValue
name|bulkSize
parameter_list|,
annotation|@
name|Nullable
name|TimeValue
name|flushInterval
parameter_list|)
block|{
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
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|concurrentRequests
operator|=
name|concurrentRequests
expr_stmt|;
name|this
operator|.
name|bulkActions
operator|=
name|bulkActions
expr_stmt|;
name|this
operator|.
name|bulkSize
operator|=
name|bulkSize
operator|.
name|bytes
argument_list|()
expr_stmt|;
name|this
operator|.
name|semaphore
operator|=
operator|new
name|Semaphore
argument_list|(
name|concurrentRequests
argument_list|)
expr_stmt|;
name|this
operator|.
name|bulkRequest
operator|=
operator|new
name|BulkRequest
argument_list|()
expr_stmt|;
name|this
operator|.
name|flushInterval
operator|=
name|flushInterval
expr_stmt|;
if|if
condition|(
name|flushInterval
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|scheduler
operator|=
operator|(
name|ScheduledThreadPoolExecutor
operator|)
name|Executors
operator|.
name|newScheduledThreadPool
argument_list|(
literal|1
argument_list|,
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
name|client
operator|.
name|settings
argument_list|()
argument_list|,
operator|(
name|name
operator|!=
literal|null
condition|?
literal|"["
operator|+
name|name
operator|+
literal|"]"
else|:
literal|""
operator|)
operator|+
literal|"bulk_processor"
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|scheduler
operator|.
name|setExecuteExistingDelayedTasksAfterShutdownPolicy
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|scheduler
operator|.
name|setContinueExistingPeriodicTasksAfterShutdownPolicy
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|scheduledFuture
operator|=
name|this
operator|.
name|scheduler
operator|.
name|scheduleWithFixedDelay
argument_list|(
operator|new
name|Flush
argument_list|()
argument_list|,
name|flushInterval
operator|.
name|millis
argument_list|()
argument_list|,
name|flushInterval
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|scheduler
operator|=
literal|null
expr_stmt|;
name|this
operator|.
name|scheduledFuture
operator|=
literal|null
expr_stmt|;
block|}
block|}
comment|/**      * Closes the processor. If flushing by time is enabled, then it's shutdown. Any remaining bulk actions are flushed.      */
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
try|try
block|{
name|awaitClose
argument_list|(
literal|0
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|exc
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
block|}
comment|/**      * Closes the processor. If flushing by time is enabled, then it's shutdown. Any remaining bulk actions are flushed.      *      * If concurrent requests are not enabled, returns {@code true} immediately.      * If concurrent requests are enabled, waits for up to the specified timeout for all bulk requests to complete then returns {@code true},      * If the specified waiting time elapses before all bulk requests complete, {@code false} is returned.      *      * @param timeout The maximum time to wait for the bulk requests to complete      * @param unit The time unit of the {@code timeout} argument      * @return {@code true} if all bulk requests completed and {@code false} if the waiting time elapsed before all the bulk requests completed      * @throws InterruptedException If the current thread is interrupted      */
DECL|method|awaitClose
specifier|public
specifier|synchronized
name|boolean
name|awaitClose
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return
literal|true
return|;
block|}
name|closed
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|scheduledFuture
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|scheduledFuture
operator|.
name|cancel
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|scheduler
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|bulkRequest
operator|.
name|numberOfActions
argument_list|()
operator|>
literal|0
condition|)
block|{
name|execute
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|concurrentRequests
operator|<
literal|1
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|semaphore
operator|.
name|tryAcquire
argument_list|(
name|this
operator|.
name|concurrentRequests
argument_list|,
name|timeout
argument_list|,
name|unit
argument_list|)
condition|)
block|{
name|semaphore
operator|.
name|release
argument_list|(
name|this
operator|.
name|concurrentRequests
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**      * Adds an {@link IndexRequest} to the list of actions to execute. Follows the same behavior of {@link IndexRequest}      * (for example, if no id is provided, one will be generated, or usage of the create flag).      */
DECL|method|add
specifier|public
name|BulkProcessor
name|add
parameter_list|(
name|IndexRequest
name|request
parameter_list|)
block|{
return|return
name|add
argument_list|(
operator|(
name|ActionRequest
operator|)
name|request
argument_list|)
return|;
block|}
comment|/**      * Adds an {@link DeleteRequest} to the list of actions to execute.      */
DECL|method|add
specifier|public
name|BulkProcessor
name|add
parameter_list|(
name|DeleteRequest
name|request
parameter_list|)
block|{
return|return
name|add
argument_list|(
operator|(
name|ActionRequest
operator|)
name|request
argument_list|)
return|;
block|}
comment|/**      * Adds either a delete or an index request.      */
DECL|method|add
specifier|public
name|BulkProcessor
name|add
parameter_list|(
name|ActionRequest
name|request
parameter_list|)
block|{
return|return
name|add
argument_list|(
name|request
argument_list|,
literal|null
argument_list|)
return|;
block|}
DECL|method|add
specifier|public
name|BulkProcessor
name|add
parameter_list|(
name|ActionRequest
name|request
parameter_list|,
annotation|@
name|Nullable
name|Object
name|payload
parameter_list|)
block|{
name|internalAdd
argument_list|(
name|request
argument_list|,
name|payload
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|isOpen
name|boolean
name|isOpen
parameter_list|()
block|{
return|return
name|closed
operator|==
literal|false
return|;
block|}
DECL|method|ensureOpen
specifier|protected
name|void
name|ensureOpen
parameter_list|()
block|{
if|if
condition|(
name|closed
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalStateException
argument_list|(
literal|"bulk process already closed"
argument_list|)
throw|;
block|}
block|}
DECL|method|internalAdd
specifier|private
specifier|synchronized
name|void
name|internalAdd
parameter_list|(
name|ActionRequest
name|request
parameter_list|,
annotation|@
name|Nullable
name|Object
name|payload
parameter_list|)
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
name|bulkRequest
operator|.
name|add
argument_list|(
name|request
argument_list|,
name|payload
argument_list|)
expr_stmt|;
name|executeIfNeeded
argument_list|()
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|BulkProcessor
name|add
parameter_list|(
name|BytesReference
name|data
parameter_list|,
name|boolean
name|contentUnsafe
parameter_list|,
annotation|@
name|Nullable
name|String
name|defaultIndex
parameter_list|,
annotation|@
name|Nullable
name|String
name|defaultType
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|add
argument_list|(
name|data
argument_list|,
name|contentUnsafe
argument_list|,
name|defaultIndex
argument_list|,
name|defaultType
argument_list|,
literal|null
argument_list|)
return|;
block|}
DECL|method|add
specifier|public
specifier|synchronized
name|BulkProcessor
name|add
parameter_list|(
name|BytesReference
name|data
parameter_list|,
name|boolean
name|contentUnsafe
parameter_list|,
annotation|@
name|Nullable
name|String
name|defaultIndex
parameter_list|,
annotation|@
name|Nullable
name|String
name|defaultType
parameter_list|,
annotation|@
name|Nullable
name|Object
name|payload
parameter_list|)
throws|throws
name|Exception
block|{
name|bulkRequest
operator|.
name|add
argument_list|(
name|data
argument_list|,
name|contentUnsafe
argument_list|,
name|defaultIndex
argument_list|,
name|defaultType
argument_list|,
literal|null
argument_list|,
name|payload
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|executeIfNeeded
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|executeIfNeeded
specifier|private
name|void
name|executeIfNeeded
parameter_list|()
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|isOverTheLimit
argument_list|()
condition|)
block|{
return|return;
block|}
name|execute
argument_list|()
expr_stmt|;
block|}
comment|// (currently) needs to be executed under a lock
DECL|method|execute
specifier|private
name|void
name|execute
parameter_list|()
block|{
specifier|final
name|BulkRequest
name|bulkRequest
init|=
name|this
operator|.
name|bulkRequest
decl_stmt|;
specifier|final
name|long
name|executionId
init|=
name|executionIdGen
operator|.
name|incrementAndGet
argument_list|()
decl_stmt|;
name|this
operator|.
name|bulkRequest
operator|=
operator|new
name|BulkRequest
argument_list|()
expr_stmt|;
if|if
condition|(
name|concurrentRequests
operator|==
literal|0
condition|)
block|{
comment|// execute in a blocking fashion...
name|boolean
name|afterCalled
init|=
literal|false
decl_stmt|;
try|try
block|{
name|listener
operator|.
name|beforeBulk
argument_list|(
name|executionId
argument_list|,
name|bulkRequest
argument_list|)
expr_stmt|;
name|BulkResponse
name|bulkItemResponses
init|=
name|client
operator|.
name|bulk
argument_list|(
name|bulkRequest
argument_list|)
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|afterCalled
operator|=
literal|true
expr_stmt|;
name|listener
operator|.
name|afterBulk
argument_list|(
name|executionId
argument_list|,
name|bulkRequest
argument_list|,
name|bulkItemResponses
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|afterCalled
condition|)
block|{
name|listener
operator|.
name|afterBulk
argument_list|(
name|executionId
argument_list|,
name|bulkRequest
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|listener
operator|.
name|beforeBulk
argument_list|(
name|executionId
argument_list|,
name|bulkRequest
argument_list|)
expr_stmt|;
name|semaphore
operator|.
name|acquire
argument_list|()
expr_stmt|;
name|client
operator|.
name|bulk
argument_list|(
name|bulkRequest
argument_list|,
operator|new
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|BulkResponse
name|response
parameter_list|)
block|{
try|try
block|{
name|listener
operator|.
name|afterBulk
argument_list|(
name|executionId
argument_list|,
name|bulkRequest
argument_list|,
name|response
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|semaphore
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
try|try
block|{
name|listener
operator|.
name|afterBulk
argument_list|(
name|executionId
argument_list|,
name|bulkRequest
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|semaphore
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
name|success
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|interrupted
argument_list|()
expr_stmt|;
name|listener
operator|.
name|afterBulk
argument_list|(
name|executionId
argument_list|,
name|bulkRequest
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|listener
operator|.
name|afterBulk
argument_list|(
name|executionId
argument_list|,
name|bulkRequest
argument_list|,
name|t
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
operator|!
name|success
condition|)
block|{
comment|// if we fail on client.bulk() release the semaphore
name|semaphore
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|isOverTheLimit
specifier|private
name|boolean
name|isOverTheLimit
parameter_list|()
block|{
if|if
condition|(
name|bulkActions
operator|!=
operator|-
literal|1
operator|&&
name|bulkRequest
operator|.
name|numberOfActions
argument_list|()
operator|>=
name|bulkActions
condition|)
block|{
return|return
literal|true
return|;
block|}
if|if
condition|(
name|bulkSize
operator|!=
operator|-
literal|1
operator|&&
name|bulkRequest
operator|.
name|estimatedSizeInBytes
argument_list|()
operator|>=
name|bulkSize
condition|)
block|{
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
comment|/**      * Flush pending delete or index requests.      */
DECL|method|flush
specifier|public
specifier|synchronized
name|void
name|flush
parameter_list|()
block|{
name|ensureOpen
argument_list|()
expr_stmt|;
if|if
condition|(
name|bulkRequest
operator|.
name|numberOfActions
argument_list|()
operator|>
literal|0
condition|)
block|{
name|execute
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|Flush
class|class
name|Flush
implements|implements
name|Runnable
block|{
annotation|@
name|Override
DECL|method|run
specifier|public
name|void
name|run
parameter_list|()
block|{
synchronized|synchronized
init|(
name|BulkProcessor
operator|.
name|this
init|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|bulkRequest
operator|.
name|numberOfActions
argument_list|()
operator|==
literal|0
condition|)
block|{
return|return;
block|}
name|execute
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

