begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.util.concurrent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
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
name|common
operator|.
name|collect
operator|.
name|Tuple
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
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
name|ArrayBlockingQueue
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
name|function
operator|.
name|Consumer
import|;
end_import

begin_comment
comment|/**  * This async IO processor allows to batch IO operations and have a single writer processing the write operations.  * This can be used to ensure that threads can continue with other work while the actual IO operation is still processed  * by a single worker. A worker in this context can be any caller of the {@link #put(Object, Consumer)} method since it will  * hijack a worker if nobody else is currently processing queued items. If the internal queue has reached it's capacity incoming threads  * might be blocked until other items are processed  */
end_comment

begin_class
DECL|class|AsyncIOProcessor
specifier|public
specifier|abstract
class|class
name|AsyncIOProcessor
parameter_list|<
name|Item
parameter_list|>
block|{
DECL|field|logger
specifier|private
specifier|final
name|Logger
name|logger
decl_stmt|;
DECL|field|queue
specifier|private
specifier|final
name|ArrayBlockingQueue
argument_list|<
name|Tuple
argument_list|<
name|Item
argument_list|,
name|Consumer
argument_list|<
name|Exception
argument_list|>
argument_list|>
argument_list|>
name|queue
decl_stmt|;
DECL|field|promiseSemaphore
specifier|private
specifier|final
name|Semaphore
name|promiseSemaphore
init|=
operator|new
name|Semaphore
argument_list|(
literal|1
argument_list|)
decl_stmt|;
DECL|method|AsyncIOProcessor
specifier|protected
name|AsyncIOProcessor
parameter_list|(
name|Logger
name|logger
parameter_list|,
name|int
name|queueSize
parameter_list|)
block|{
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
name|this
operator|.
name|queue
operator|=
operator|new
name|ArrayBlockingQueue
argument_list|<>
argument_list|(
name|queueSize
argument_list|)
expr_stmt|;
block|}
comment|/**      * Adds the given item to the queue. The listener is notified once the item is processed      */
DECL|method|put
specifier|public
specifier|final
name|void
name|put
parameter_list|(
name|Item
name|item
parameter_list|,
name|Consumer
argument_list|<
name|Exception
argument_list|>
name|listener
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|item
argument_list|,
literal|"item must not be null"
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|listener
argument_list|,
literal|"listener must not be null"
argument_list|)
expr_stmt|;
comment|// the algorithm here tires to reduce the load on each individual caller.
comment|// we try to have only one caller that processes pending items to disc while others just add to the queue but
comment|// at the same time never overload the node by pushing too many items into the queue.
comment|// we first try make a promise that we are responsible for the processing
specifier|final
name|boolean
name|promised
init|=
name|promiseSemaphore
operator|.
name|tryAcquire
argument_list|()
decl_stmt|;
specifier|final
name|Tuple
argument_list|<
name|Item
argument_list|,
name|Consumer
argument_list|<
name|Exception
argument_list|>
argument_list|>
name|itemTuple
init|=
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|item
argument_list|,
name|listener
argument_list|)
decl_stmt|;
if|if
condition|(
name|promised
operator|==
literal|false
condition|)
block|{
comment|// in this case we are not responsible and can just block until there is space
try|try
block|{
name|queue
operator|.
name|put
argument_list|(
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|item
argument_list|,
name|listener
argument_list|)
argument_list|)
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
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
name|listener
operator|.
name|accept
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|// here we have to try to make the promise again otherwise there is a race when a thread puts an entry without making the promise
comment|// while we are draining that mean we might exit below too early in the while loop if the drainAndSync call is fast.
if|if
condition|(
name|promised
operator|||
name|promiseSemaphore
operator|.
name|tryAcquire
argument_list|()
condition|)
block|{
specifier|final
name|List
argument_list|<
name|Tuple
argument_list|<
name|Item
argument_list|,
name|Consumer
argument_list|<
name|Exception
argument_list|>
argument_list|>
argument_list|>
name|candidates
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
block|{
if|if
condition|(
name|promised
condition|)
block|{
comment|// we are responsible for processing we don't need to add the tuple to the queue we can just add it to the candidates
name|candidates
operator|.
name|add
argument_list|(
name|itemTuple
argument_list|)
expr_stmt|;
block|}
comment|// since we made the promise to process we gotta do it here at least once
name|drainAndProcess
argument_list|(
name|candidates
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|promiseSemaphore
operator|.
name|release
argument_list|()
expr_stmt|;
comment|// now to ensure we are passing it on we release the promise so another thread can take over
block|}
while|while
condition|(
name|queue
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
operator|&&
name|promiseSemaphore
operator|.
name|tryAcquire
argument_list|()
condition|)
block|{
comment|// yet if the queue is not empty AND nobody else has yet made the promise to take over we continue processing
try|try
block|{
name|drainAndProcess
argument_list|(
name|candidates
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|promiseSemaphore
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|method|drainAndProcess
specifier|private
name|void
name|drainAndProcess
parameter_list|(
name|List
argument_list|<
name|Tuple
argument_list|<
name|Item
argument_list|,
name|Consumer
argument_list|<
name|Exception
argument_list|>
argument_list|>
argument_list|>
name|candidates
parameter_list|)
block|{
name|queue
operator|.
name|drainTo
argument_list|(
name|candidates
argument_list|)
expr_stmt|;
name|processList
argument_list|(
name|candidates
argument_list|)
expr_stmt|;
name|candidates
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
DECL|method|processList
specifier|private
name|void
name|processList
parameter_list|(
name|List
argument_list|<
name|Tuple
argument_list|<
name|Item
argument_list|,
name|Consumer
argument_list|<
name|Exception
argument_list|>
argument_list|>
argument_list|>
name|candidates
parameter_list|)
block|{
name|Exception
name|exception
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|candidates
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
try|try
block|{
name|write
argument_list|(
name|candidates
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
comment|// if this fails we are in deep shit - fail the request
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to write candidates"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
comment|// this exception is passed to all listeners - we don't retry. if this doesn't work we are in deep shit
name|exception
operator|=
name|ex
expr_stmt|;
block|}
block|}
for|for
control|(
name|Tuple
argument_list|<
name|Item
argument_list|,
name|Consumer
argument_list|<
name|Exception
argument_list|>
argument_list|>
name|tuple
range|:
name|candidates
control|)
block|{
name|Consumer
argument_list|<
name|Exception
argument_list|>
name|consumer
init|=
name|tuple
operator|.
name|v2
argument_list|()
decl_stmt|;
try|try
block|{
name|consumer
operator|.
name|accept
argument_list|(
name|exception
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to notify callback"
argument_list|,
name|ex
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Writes or processes the items out or to disk.      */
DECL|method|write
specifier|protected
specifier|abstract
name|void
name|write
parameter_list|(
name|List
argument_list|<
name|Tuple
argument_list|<
name|Item
argument_list|,
name|Consumer
argument_list|<
name|Exception
argument_list|>
argument_list|>
argument_list|>
name|candidates
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit
