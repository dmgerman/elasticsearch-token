begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.disruption
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|disruption
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
name|SuppressForbidden
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
name|AbstractRunnable
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
name|InternalTestCluster
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
name|Random
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
name|atomic
operator|.
name|AtomicReference
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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

begin_comment
comment|/**  * Suspends all threads on the specified node in order to simulate a long gc.  */
end_comment

begin_class
DECL|class|LongGCDisruption
specifier|public
class|class
name|LongGCDisruption
extends|extends
name|SingleNodeDisruption
block|{
DECL|field|unsafeClasses
specifier|private
specifier|static
specifier|final
name|Pattern
index|[]
name|unsafeClasses
init|=
operator|new
name|Pattern
index|[]
block|{
comment|// logging has shared JVM locks - we may suspend a thread and block other nodes from doing their thing
name|Pattern
operator|.
name|compile
argument_list|(
literal|"logging\\.log4j"
argument_list|)
block|}
decl_stmt|;
DECL|field|disruptedNode
specifier|protected
specifier|final
name|String
name|disruptedNode
decl_stmt|;
DECL|field|suspendedThreads
specifier|private
name|Set
argument_list|<
name|Thread
argument_list|>
name|suspendedThreads
decl_stmt|;
DECL|method|LongGCDisruption
specifier|public
name|LongGCDisruption
parameter_list|(
name|Random
name|random
parameter_list|,
name|String
name|disruptedNode
parameter_list|)
block|{
name|super
argument_list|(
name|random
argument_list|)
expr_stmt|;
name|this
operator|.
name|disruptedNode
operator|=
name|disruptedNode
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|startDisrupting
specifier|public
specifier|synchronized
name|void
name|startDisrupting
parameter_list|()
block|{
if|if
condition|(
name|suspendedThreads
operator|==
literal|null
condition|)
block|{
name|boolean
name|success
init|=
literal|false
decl_stmt|;
try|try
block|{
name|suspendedThreads
operator|=
name|ConcurrentHashMap
operator|.
name|newKeySet
argument_list|()
expr_stmt|;
specifier|final
name|String
name|currentThreadName
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
assert|assert
name|currentThreadName
operator|.
name|contains
argument_list|(
literal|"["
operator|+
name|disruptedNode
operator|+
literal|"]"
argument_list|)
operator|==
literal|false
operator|:
literal|"current thread match pattern. thread name: "
operator|+
name|currentThreadName
operator|+
literal|", node: "
operator|+
name|disruptedNode
assert|;
comment|// we spawn a background thread to protect against deadlock which can happen
comment|// if there are shared resources between caller thread and and suspended threads
comment|// see unsafeClasses to how to avoid that
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|stoppingError
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|Thread
name|stoppingThread
init|=
operator|new
name|Thread
argument_list|(
operator|new
name|AbstractRunnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|stoppingError
operator|.
name|set
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doRun
parameter_list|()
throws|throws
name|Exception
block|{
comment|// keep trying to stop threads, until no new threads are discovered.
while|while
condition|(
name|stopNodeThreads
argument_list|(
name|disruptedNode
argument_list|,
name|suspendedThreads
argument_list|)
condition|)
block|{
if|if
condition|(
name|Thread
operator|.
name|interrupted
argument_list|()
condition|)
block|{
return|return;
block|}
block|}
block|}
block|}
argument_list|)
decl_stmt|;
name|stoppingThread
operator|.
name|setName
argument_list|(
name|currentThreadName
operator|+
literal|"[LongGCDisruption][threadStopper]"
argument_list|)
expr_stmt|;
name|stoppingThread
operator|.
name|start
argument_list|()
expr_stmt|;
try|try
block|{
name|stoppingThread
operator|.
name|join
argument_list|(
name|getStoppingTimeoutInMillis
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|stoppingThread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
comment|// best effort to signal stopping
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
if|if
condition|(
name|stoppingError
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"unknown error while stopping threads"
argument_list|,
name|stoppingError
operator|.
name|get
argument_list|()
argument_list|)
throw|;
block|}
if|if
condition|(
name|stoppingThread
operator|.
name|isAlive
argument_list|()
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to stop node [{}]'s threads within [{}] millis. Stopping thread stack trace:\n {}"
argument_list|,
name|disruptedNode
argument_list|,
name|getStoppingTimeoutInMillis
argument_list|()
argument_list|,
name|stackTrace
argument_list|(
name|stoppingThread
argument_list|)
argument_list|)
expr_stmt|;
name|stoppingThread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
comment|// best effort;
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"stopping node threads took too long"
argument_list|)
throw|;
block|}
name|success
operator|=
literal|true
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|success
operator|==
literal|false
condition|)
block|{
comment|// resume threads if failed
name|resumeThreads
argument_list|(
name|suspendedThreads
argument_list|)
expr_stmt|;
name|suspendedThreads
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"can't disrupt twice, call stopDisrupting() first"
argument_list|)
throw|;
block|}
block|}
DECL|method|stackTrace
specifier|private
name|String
name|stackTrace
parameter_list|(
name|Thread
name|thread
parameter_list|)
block|{
return|return
name|Arrays
operator|.
name|stream
argument_list|(
name|thread
operator|.
name|getStackTrace
argument_list|()
argument_list|)
operator|.
name|map
argument_list|(
name|Object
operator|::
name|toString
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|joining
argument_list|(
literal|"\n"
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|stopDisrupting
specifier|public
specifier|synchronized
name|void
name|stopDisrupting
parameter_list|()
block|{
if|if
condition|(
name|suspendedThreads
operator|!=
literal|null
condition|)
block|{
name|resumeThreads
argument_list|(
name|suspendedThreads
argument_list|)
expr_stmt|;
name|suspendedThreads
operator|=
literal|null
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|removeAndEnsureHealthy
specifier|public
name|void
name|removeAndEnsureHealthy
parameter_list|(
name|InternalTestCluster
name|cluster
parameter_list|)
block|{
name|removeFromCluster
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
name|ensureNodeCount
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|expectedTimeToHeal
specifier|public
name|TimeValue
name|expectedTimeToHeal
parameter_list|()
block|{
return|return
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|0
argument_list|)
return|;
block|}
comment|/**      * resolves all threads belonging to given node and suspends them if their current stack trace      * is "safe". Threads are added to nodeThreads if suspended.      *      * returns true if some live threads were found. The caller is expected to call this method      * until no more "live" are found.      */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
comment|// stops/resumes threads intentionally
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"stops/resumes threads intentionally"
argument_list|)
DECL|method|stopNodeThreads
specifier|protected
name|boolean
name|stopNodeThreads
parameter_list|(
name|String
name|node
parameter_list|,
name|Set
argument_list|<
name|Thread
argument_list|>
name|nodeThreads
parameter_list|)
block|{
name|Thread
index|[]
name|allThreads
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|allThreads
operator|==
literal|null
condition|)
block|{
name|allThreads
operator|=
operator|new
name|Thread
index|[
name|Thread
operator|.
name|activeCount
argument_list|()
index|]
expr_stmt|;
if|if
condition|(
name|Thread
operator|.
name|enumerate
argument_list|(
name|allThreads
argument_list|)
operator|>
name|allThreads
operator|.
name|length
condition|)
block|{
comment|// we didn't make enough space, retry
name|allThreads
operator|=
literal|null
expr_stmt|;
block|}
block|}
name|boolean
name|liveThreadsFound
init|=
literal|false
decl_stmt|;
specifier|final
name|String
name|nodeThreadNamePart
init|=
literal|"["
operator|+
name|node
operator|+
literal|"]"
decl_stmt|;
for|for
control|(
name|Thread
name|thread
range|:
name|allThreads
control|)
block|{
if|if
condition|(
name|thread
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|String
name|name
init|=
name|thread
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|name
operator|.
name|contains
argument_list|(
name|nodeThreadNamePart
argument_list|)
condition|)
block|{
if|if
condition|(
name|thread
operator|.
name|isAlive
argument_list|()
operator|&&
name|nodeThreads
operator|.
name|add
argument_list|(
name|thread
argument_list|)
condition|)
block|{
name|liveThreadsFound
operator|=
literal|true
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"stopping thread [{}]"
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|thread
operator|.
name|suspend
argument_list|()
expr_stmt|;
comment|// double check the thread is not in a shared resource like logging. If so, let it go and come back..
name|boolean
name|safe
init|=
literal|true
decl_stmt|;
name|safe
label|:
for|for
control|(
name|StackTraceElement
name|stackElement
range|:
name|thread
operator|.
name|getStackTrace
argument_list|()
control|)
block|{
name|String
name|className
init|=
name|stackElement
operator|.
name|getClassName
argument_list|()
decl_stmt|;
for|for
control|(
name|Pattern
name|unsafePattern
range|:
name|getUnsafeClasses
argument_list|()
control|)
block|{
if|if
condition|(
name|unsafePattern
operator|.
name|matcher
argument_list|(
name|className
argument_list|)
operator|.
name|find
argument_list|()
condition|)
block|{
name|safe
operator|=
literal|false
expr_stmt|;
break|break
name|safe
break|;
block|}
block|}
block|}
if|if
condition|(
operator|!
name|safe
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"resuming thread [{}] as it is in a critical section"
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|thread
operator|.
name|resume
argument_list|()
expr_stmt|;
name|nodeThreads
operator|.
name|remove
argument_list|(
name|thread
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
return|return
name|liveThreadsFound
return|;
block|}
comment|// for testing
DECL|method|getUnsafeClasses
specifier|protected
name|Pattern
index|[]
name|getUnsafeClasses
parameter_list|()
block|{
return|return
name|unsafeClasses
return|;
block|}
comment|// for testing
DECL|method|getStoppingTimeoutInMillis
specifier|protected
name|long
name|getStoppingTimeoutInMillis
parameter_list|()
block|{
return|return
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|30
argument_list|)
operator|.
name|getMillis
argument_list|()
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"deprecation"
argument_list|)
comment|// stops/resumes threads intentionally
annotation|@
name|SuppressForbidden
argument_list|(
name|reason
operator|=
literal|"stops/resumes threads intentionally"
argument_list|)
DECL|method|resumeThreads
specifier|protected
name|void
name|resumeThreads
parameter_list|(
name|Set
argument_list|<
name|Thread
argument_list|>
name|threads
parameter_list|)
block|{
for|for
control|(
name|Thread
name|thread
range|:
name|threads
control|)
block|{
name|thread
operator|.
name|resume
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

