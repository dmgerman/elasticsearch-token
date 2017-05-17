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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|BlockingQueue
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
name|ThreadFactory
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
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Stream
import|;
end_import

begin_comment
comment|/**  * An extension to thread pool executor, allowing (in the future) to add specific additional stats to it.  */
end_comment

begin_class
DECL|class|EsThreadPoolExecutor
specifier|public
class|class
name|EsThreadPoolExecutor
extends|extends
name|ThreadPoolExecutor
block|{
DECL|field|contextHolder
specifier|private
specifier|final
name|ThreadContext
name|contextHolder
decl_stmt|;
DECL|field|listener
specifier|private
specifier|volatile
name|ShutdownListener
name|listener
decl_stmt|;
DECL|field|monitor
specifier|private
specifier|final
name|Object
name|monitor
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
comment|/**      * Name used in error reporting.      */
DECL|field|name
specifier|protected
specifier|final
name|String
name|name
decl_stmt|;
DECL|method|EsThreadPoolExecutor
name|EsThreadPoolExecutor
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|corePoolSize
parameter_list|,
name|int
name|maximumPoolSize
parameter_list|,
name|long
name|keepAliveTime
parameter_list|,
name|TimeUnit
name|unit
parameter_list|,
name|BlockingQueue
argument_list|<
name|Runnable
argument_list|>
name|workQueue
parameter_list|,
name|ThreadFactory
name|threadFactory
parameter_list|,
name|ThreadContext
name|contextHolder
parameter_list|)
block|{
name|this
argument_list|(
name|name
argument_list|,
name|corePoolSize
argument_list|,
name|maximumPoolSize
argument_list|,
name|keepAliveTime
argument_list|,
name|unit
argument_list|,
name|workQueue
argument_list|,
name|threadFactory
argument_list|,
operator|new
name|EsAbortPolicy
argument_list|()
argument_list|,
name|contextHolder
argument_list|)
expr_stmt|;
block|}
DECL|method|EsThreadPoolExecutor
name|EsThreadPoolExecutor
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|corePoolSize
parameter_list|,
name|int
name|maximumPoolSize
parameter_list|,
name|long
name|keepAliveTime
parameter_list|,
name|TimeUnit
name|unit
parameter_list|,
name|BlockingQueue
argument_list|<
name|Runnable
argument_list|>
name|workQueue
parameter_list|,
name|ThreadFactory
name|threadFactory
parameter_list|,
name|XRejectedExecutionHandler
name|handler
parameter_list|,
name|ThreadContext
name|contextHolder
parameter_list|)
block|{
name|super
argument_list|(
name|corePoolSize
argument_list|,
name|maximumPoolSize
argument_list|,
name|keepAliveTime
argument_list|,
name|unit
argument_list|,
name|workQueue
argument_list|,
name|threadFactory
argument_list|,
name|handler
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|contextHolder
operator|=
name|contextHolder
expr_stmt|;
block|}
DECL|method|shutdown
specifier|public
name|void
name|shutdown
parameter_list|(
name|ShutdownListener
name|listener
parameter_list|)
block|{
synchronized|synchronized
init|(
name|monitor
init|)
block|{
if|if
condition|(
name|this
operator|.
name|listener
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Shutdown was already called on this thread pool"
argument_list|)
throw|;
block|}
if|if
condition|(
name|isTerminated
argument_list|()
condition|)
block|{
name|listener
operator|.
name|onTerminated
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
block|}
block|}
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|terminated
specifier|protected
specifier|synchronized
name|void
name|terminated
parameter_list|()
block|{
name|super
operator|.
name|terminated
argument_list|()
expr_stmt|;
synchronized|synchronized
init|(
name|monitor
init|)
block|{
if|if
condition|(
name|listener
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|listener
operator|.
name|onTerminated
argument_list|()
expr_stmt|;
block|}
finally|finally
block|{
name|listener
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
block|}
DECL|interface|ShutdownListener
specifier|public
interface|interface
name|ShutdownListener
block|{
DECL|method|onTerminated
name|void
name|onTerminated
parameter_list|()
function_decl|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
specifier|final
name|Runnable
name|command
parameter_list|)
block|{
name|doExecute
argument_list|(
name|wrapRunnable
argument_list|(
name|command
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
specifier|final
name|Runnable
name|command
parameter_list|)
block|{
try|try
block|{
name|super
operator|.
name|execute
argument_list|(
name|command
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|EsRejectedExecutionException
name|ex
parameter_list|)
block|{
if|if
condition|(
name|command
operator|instanceof
name|AbstractRunnable
condition|)
block|{
comment|// If we are an abstract runnable we can handle the rejection
comment|// directly and don't need to rethrow it.
try|try
block|{
operator|(
operator|(
name|AbstractRunnable
operator|)
name|command
operator|)
operator|.
name|onRejection
argument_list|(
name|ex
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
operator|(
operator|(
name|AbstractRunnable
operator|)
name|command
operator|)
operator|.
name|onAfter
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
name|ex
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|afterExecute
specifier|protected
name|void
name|afterExecute
parameter_list|(
name|Runnable
name|r
parameter_list|,
name|Throwable
name|t
parameter_list|)
block|{
name|super
operator|.
name|afterExecute
argument_list|(
name|r
argument_list|,
name|t
argument_list|)
expr_stmt|;
assert|assert
name|assertDefaultContext
argument_list|(
name|r
argument_list|)
assert|;
block|}
DECL|method|assertDefaultContext
specifier|private
name|boolean
name|assertDefaultContext
parameter_list|(
name|Runnable
name|r
parameter_list|)
block|{
try|try
block|{
assert|assert
name|contextHolder
operator|.
name|isDefaultContext
argument_list|()
operator|:
literal|"the thread context is not the default context and the thread ["
operator|+
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
operator|+
literal|"] is being returned to the pool after executing ["
operator|+
name|r
operator|+
literal|"]"
assert|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|ex
parameter_list|)
block|{
comment|// sometimes we execute on a closed context and isDefaultContext doen't bypass the ensureOpen checks
comment|// this must not trigger an exception here since we only assert if the default is restored and
comment|// we don't really care if we are closed
if|if
condition|(
name|contextHolder
operator|.
name|isClosed
argument_list|()
operator|==
literal|false
condition|)
block|{
throw|throw
name|ex
throw|;
block|}
block|}
return|return
literal|true
return|;
block|}
comment|/**      * Returns a stream of all pending tasks. This is similar to {@link #getQueue()} but will expose the originally submitted      * {@link Runnable} instances rather than potentially wrapped ones.      */
DECL|method|getTasks
specifier|public
name|Stream
argument_list|<
name|Runnable
argument_list|>
name|getTasks
parameter_list|()
block|{
return|return
name|this
operator|.
name|getQueue
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|this
operator|::
name|unwrap
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|b
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|b
operator|.
name|append
argument_list|(
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|'['
argument_list|)
expr_stmt|;
name|b
operator|.
name|append
argument_list|(
name|name
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
if|if
condition|(
name|getQueue
argument_list|()
operator|instanceof
name|SizeBlockingQueue
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|SizeBlockingQueue
name|queue
init|=
operator|(
name|SizeBlockingQueue
operator|)
name|getQueue
argument_list|()
decl_stmt|;
name|b
operator|.
name|append
argument_list|(
literal|"queue capacity = "
argument_list|)
operator|.
name|append
argument_list|(
name|queue
operator|.
name|capacity
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
comment|/*          * ThreadPoolExecutor has some nice information in its toString but we          * can't get at it easily without just getting the toString.          */
name|b
operator|.
name|append
argument_list|(
name|super
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|']'
argument_list|)
expr_stmt|;
return|return
name|b
operator|.
name|toString
argument_list|()
return|;
block|}
DECL|method|wrapRunnable
specifier|protected
name|Runnable
name|wrapRunnable
parameter_list|(
name|Runnable
name|command
parameter_list|)
block|{
return|return
name|contextHolder
operator|.
name|preserveContext
argument_list|(
name|command
argument_list|)
return|;
block|}
DECL|method|unwrap
specifier|protected
name|Runnable
name|unwrap
parameter_list|(
name|Runnable
name|runnable
parameter_list|)
block|{
return|return
name|contextHolder
operator|.
name|unwrap
argument_list|(
name|runnable
argument_list|)
return|;
block|}
block|}
end_class

end_unit

