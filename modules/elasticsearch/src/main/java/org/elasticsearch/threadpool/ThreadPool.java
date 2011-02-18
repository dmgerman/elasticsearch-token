begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.threadpool
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|collect
operator|.
name|ImmutableMap
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
name|Maps
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
name|component
operator|.
name|AbstractComponent
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
name|inject
operator|.
name|Inject
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
name|ImmutableSettings
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
name|SizeValue
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
name|DynamicExecutors
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
name|MoreExecutors
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
name|jsr166y
operator|.
name|LinkedTransferQueue
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
name|*
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
name|*
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
name|unit
operator|.
name|TimeValue
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ThreadPool
specifier|public
class|class
name|ThreadPool
extends|extends
name|AbstractComponent
block|{
DECL|class|Names
specifier|public
specifier|static
class|class
name|Names
block|{
DECL|field|SAME
specifier|public
specifier|static
specifier|final
name|String
name|SAME
init|=
literal|"same"
decl_stmt|;
DECL|field|CACHED
specifier|public
specifier|static
specifier|final
name|String
name|CACHED
init|=
literal|"cached"
decl_stmt|;
DECL|field|INDEX
specifier|public
specifier|static
specifier|final
name|String
name|INDEX
init|=
literal|"index"
decl_stmt|;
DECL|field|SEARCH
specifier|public
specifier|static
specifier|final
name|String
name|SEARCH
init|=
literal|"search"
decl_stmt|;
DECL|field|PERCOLATE
specifier|public
specifier|static
specifier|final
name|String
name|PERCOLATE
init|=
literal|"percolate"
decl_stmt|;
DECL|field|MANAGEMENT
specifier|public
specifier|static
specifier|final
name|String
name|MANAGEMENT
init|=
literal|"management"
decl_stmt|;
DECL|field|SNAPSHOT
specifier|public
specifier|static
specifier|final
name|String
name|SNAPSHOT
init|=
literal|"snapshot"
decl_stmt|;
block|}
DECL|field|executors
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Executor
argument_list|>
name|executors
decl_stmt|;
DECL|field|scheduler
specifier|private
specifier|final
name|ScheduledExecutorService
name|scheduler
decl_stmt|;
DECL|method|ThreadPool
specifier|public
name|ThreadPool
parameter_list|()
block|{
name|this
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
expr_stmt|;
block|}
DECL|method|ThreadPool
annotation|@
name|Inject
specifier|public
name|ThreadPool
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|groupSettings
init|=
name|settings
operator|.
name|getGroups
argument_list|(
literal|"threadpool"
argument_list|)
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Executor
argument_list|>
name|executors
init|=
name|Maps
operator|.
name|newHashMap
argument_list|()
decl_stmt|;
name|executors
operator|.
name|put
argument_list|(
name|Names
operator|.
name|CACHED
argument_list|,
name|build
argument_list|(
name|Names
operator|.
name|CACHED
argument_list|,
literal|"cached"
argument_list|,
name|groupSettings
operator|.
name|get
argument_list|(
name|Names
operator|.
name|CACHED
argument_list|)
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"keep_alive"
argument_list|,
literal|"30s"
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|executors
operator|.
name|put
argument_list|(
name|Names
operator|.
name|INDEX
argument_list|,
name|build
argument_list|(
name|Names
operator|.
name|INDEX
argument_list|,
literal|"cached"
argument_list|,
name|groupSettings
operator|.
name|get
argument_list|(
name|Names
operator|.
name|INDEX
argument_list|)
argument_list|,
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|)
expr_stmt|;
name|executors
operator|.
name|put
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|,
name|build
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|,
literal|"cached"
argument_list|,
name|groupSettings
operator|.
name|get
argument_list|(
name|Names
operator|.
name|SEARCH
argument_list|)
argument_list|,
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|)
expr_stmt|;
name|executors
operator|.
name|put
argument_list|(
name|Names
operator|.
name|PERCOLATE
argument_list|,
name|build
argument_list|(
name|Names
operator|.
name|PERCOLATE
argument_list|,
literal|"cached"
argument_list|,
name|groupSettings
operator|.
name|get
argument_list|(
name|Names
operator|.
name|PERCOLATE
argument_list|)
argument_list|,
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|)
expr_stmt|;
name|executors
operator|.
name|put
argument_list|(
name|Names
operator|.
name|MANAGEMENT
argument_list|,
name|build
argument_list|(
name|Names
operator|.
name|MANAGEMENT
argument_list|,
literal|"scaling"
argument_list|,
name|groupSettings
operator|.
name|get
argument_list|(
name|Names
operator|.
name|MANAGEMENT
argument_list|)
argument_list|,
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"keep_alive"
argument_list|,
literal|"30s"
argument_list|)
operator|.
name|put
argument_list|(
literal|"size"
argument_list|,
literal|20
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|executors
operator|.
name|put
argument_list|(
name|Names
operator|.
name|SNAPSHOT
argument_list|,
name|build
argument_list|(
name|Names
operator|.
name|SNAPSHOT
argument_list|,
literal|"scaling"
argument_list|,
name|groupSettings
operator|.
name|get
argument_list|(
name|Names
operator|.
name|SNAPSHOT
argument_list|)
argument_list|,
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
argument_list|)
expr_stmt|;
name|executors
operator|.
name|put
argument_list|(
name|Names
operator|.
name|SAME
argument_list|,
name|MoreExecutors
operator|.
name|sameThreadExecutor
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|executors
operator|=
name|ImmutableMap
operator|.
name|copyOf
argument_list|(
name|executors
argument_list|)
expr_stmt|;
name|this
operator|.
name|scheduler
operator|=
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
name|settings
argument_list|,
literal|"[scheduler]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|cached
specifier|public
name|Executor
name|cached
parameter_list|()
block|{
return|return
name|executor
argument_list|(
name|Names
operator|.
name|CACHED
argument_list|)
return|;
block|}
DECL|method|executor
specifier|public
name|Executor
name|executor
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|Executor
name|executor
init|=
name|executors
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|executor
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"No executor found for ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
return|return
name|executor
return|;
block|}
DECL|method|scheduleWithFixedDelay
specifier|public
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|scheduleWithFixedDelay
parameter_list|(
name|Runnable
name|command
parameter_list|,
name|TimeValue
name|interval
parameter_list|)
block|{
return|return
name|scheduler
operator|.
name|scheduleWithFixedDelay
argument_list|(
operator|new
name|LoggingRunnable
argument_list|(
name|command
argument_list|)
argument_list|,
name|interval
operator|.
name|millis
argument_list|()
argument_list|,
name|interval
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
DECL|method|schedule
specifier|public
name|ScheduledFuture
argument_list|<
name|?
argument_list|>
name|schedule
parameter_list|(
name|TimeValue
name|delay
parameter_list|,
name|String
name|name
parameter_list|,
name|Runnable
name|command
parameter_list|)
block|{
if|if
condition|(
operator|!
name|Names
operator|.
name|SAME
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
name|command
operator|=
operator|new
name|ThreadedRunnable
argument_list|(
name|command
argument_list|,
name|executor
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|scheduler
operator|.
name|schedule
argument_list|(
name|command
argument_list|,
name|delay
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
DECL|method|shutdown
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
name|scheduler
operator|.
name|shutdown
argument_list|()
expr_stmt|;
for|for
control|(
name|Executor
name|executor
range|:
name|executors
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|executor
operator|instanceof
name|ThreadPoolExecutor
condition|)
block|{
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|executor
operator|)
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|awaitTermination
specifier|public
name|boolean
name|awaitTermination
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
name|boolean
name|result
init|=
name|scheduler
operator|.
name|awaitTermination
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
decl_stmt|;
for|for
control|(
name|Executor
name|executor
range|:
name|executors
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|executor
operator|instanceof
name|ThreadPoolExecutor
condition|)
block|{
name|result
operator|&=
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|executor
operator|)
operator|.
name|awaitTermination
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
DECL|method|shutdownNow
specifier|public
name|void
name|shutdownNow
parameter_list|()
block|{
name|scheduler
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
for|for
control|(
name|Executor
name|executor
range|:
name|executors
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|executor
operator|instanceof
name|ThreadPoolExecutor
condition|)
block|{
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|executor
operator|)
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
block|}
block|}
block|}
DECL|method|build
specifier|private
name|Executor
name|build
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|defaultType
parameter_list|,
annotation|@
name|Nullable
name|Settings
name|settings
parameter_list|,
name|Settings
name|defaultSettings
parameter_list|)
block|{
if|if
condition|(
name|settings
operator|==
literal|null
condition|)
block|{
name|settings
operator|=
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
expr_stmt|;
block|}
name|String
name|type
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"type"
argument_list|,
name|defaultType
argument_list|)
decl_stmt|;
name|ThreadFactory
name|threadFactory
init|=
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
literal|"["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"same"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"creating thread_pool [{}], type [{}]"
argument_list|,
name|name
argument_list|,
name|type
argument_list|)
expr_stmt|;
return|return
name|MoreExecutors
operator|.
name|sameThreadExecutor
argument_list|()
return|;
block|}
elseif|else
if|if
condition|(
literal|"cached"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|TimeValue
name|keepAlive
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
literal|"keep_alive"
argument_list|,
name|defaultSettings
operator|.
name|getAsTime
argument_list|(
literal|"keep_alive"
argument_list|,
name|timeValueMinutes
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"creating thread_pool [{}], type [{}], keep_alive [{}]"
argument_list|,
name|name
argument_list|,
name|type
argument_list|,
name|keepAlive
argument_list|)
expr_stmt|;
return|return
operator|new
name|ThreadPoolExecutor
argument_list|(
literal|0
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|,
name|keepAlive
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
operator|new
name|SynchronousQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|,
name|threadFactory
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"fixed"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|int
name|size
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"size"
argument_list|,
name|defaultSettings
operator|.
name|getAsInt
argument_list|(
literal|"size"
argument_list|,
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
operator|*
literal|5
argument_list|)
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"creating thread_pool [{}], type [{}], size [{}]"
argument_list|,
name|name
argument_list|,
name|type
argument_list|,
name|size
argument_list|)
expr_stmt|;
return|return
operator|new
name|ThreadPoolExecutor
argument_list|(
name|size
argument_list|,
name|size
argument_list|,
literal|0L
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
operator|new
name|LinkedTransferQueue
argument_list|<
name|Runnable
argument_list|>
argument_list|()
argument_list|,
name|threadFactory
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"scaling"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|TimeValue
name|keepAlive
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
literal|"keep_alive"
argument_list|,
name|defaultSettings
operator|.
name|getAsTime
argument_list|(
literal|"keep_alive"
argument_list|,
name|timeValueMinutes
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|min
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"min"
argument_list|,
name|defaultSettings
operator|.
name|getAsInt
argument_list|(
literal|"min"
argument_list|,
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"size"
argument_list|,
name|defaultSettings
operator|.
name|getAsInt
argument_list|(
literal|"size"
argument_list|,
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
operator|*
literal|5
argument_list|)
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"creating thread_pool [{}], type [{}], min [{}], size [{}], keep_alive [{}]"
argument_list|,
name|name
argument_list|,
name|type
argument_list|,
name|min
argument_list|,
name|size
argument_list|,
name|keepAlive
argument_list|)
expr_stmt|;
return|return
name|DynamicExecutors
operator|.
name|newScalingThreadPool
argument_list|(
name|min
argument_list|,
name|size
argument_list|,
name|keepAlive
operator|.
name|millis
argument_list|()
argument_list|,
name|threadFactory
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
literal|"blocking"
operator|.
name|equals
argument_list|(
name|type
argument_list|)
condition|)
block|{
name|TimeValue
name|keepAlive
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
literal|"keep_alive"
argument_list|,
name|defaultSettings
operator|.
name|getAsTime
argument_list|(
literal|"keep_alive"
argument_list|,
name|timeValueMinutes
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|min
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"min"
argument_list|,
name|defaultSettings
operator|.
name|getAsInt
argument_list|(
literal|"min"
argument_list|,
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|int
name|size
init|=
name|settings
operator|.
name|getAsInt
argument_list|(
literal|"size"
argument_list|,
name|defaultSettings
operator|.
name|getAsInt
argument_list|(
literal|"size"
argument_list|,
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
operator|*
literal|5
argument_list|)
argument_list|)
decl_stmt|;
name|SizeValue
name|capacity
init|=
name|settings
operator|.
name|getAsSize
argument_list|(
literal|"capacity"
argument_list|,
name|defaultSettings
operator|.
name|getAsSize
argument_list|(
literal|"capacity"
argument_list|,
operator|new
name|SizeValue
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|TimeValue
name|waitTime
init|=
name|settings
operator|.
name|getAsTime
argument_list|(
literal|"wait_time"
argument_list|,
name|defaultSettings
operator|.
name|getAsTime
argument_list|(
literal|"wait_time"
argument_list|,
name|timeValueSeconds
argument_list|(
literal|60
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"creating thread_pool [{}], type [{}], min [{}], size [{}], keep_alive [{}], wait_time [{}]"
argument_list|,
name|name
argument_list|,
name|type
argument_list|,
name|min
argument_list|,
name|size
argument_list|,
name|keepAlive
argument_list|,
name|waitTime
argument_list|)
expr_stmt|;
return|return
name|DynamicExecutors
operator|.
name|newBlockingThreadPool
argument_list|(
name|min
argument_list|,
name|size
argument_list|,
name|keepAlive
operator|.
name|millis
argument_list|()
argument_list|,
operator|(
name|int
operator|)
name|capacity
operator|.
name|singles
argument_list|()
argument_list|,
name|waitTime
operator|.
name|millis
argument_list|()
argument_list|,
name|threadFactory
argument_list|)
return|;
block|}
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"No type found ["
operator|+
name|type
operator|+
literal|"], for ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
DECL|class|LoggingRunnable
class|class
name|LoggingRunnable
implements|implements
name|Runnable
block|{
DECL|field|runnable
specifier|private
specifier|final
name|Runnable
name|runnable
decl_stmt|;
DECL|method|LoggingRunnable
name|LoggingRunnable
parameter_list|(
name|Runnable
name|runnable
parameter_list|)
block|{
name|this
operator|.
name|runnable
operator|=
name|runnable
expr_stmt|;
block|}
DECL|method|run
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|runnable
operator|.
name|run
argument_list|()
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
literal|"failed to run {}"
argument_list|,
name|e
argument_list|,
name|runnable
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|hashCode
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|runnable
operator|.
name|hashCode
argument_list|()
return|;
block|}
DECL|method|equals
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
name|runnable
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
return|;
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"[threaded] "
operator|+
name|runnable
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
DECL|class|ThreadedRunnable
class|class
name|ThreadedRunnable
implements|implements
name|Runnable
block|{
DECL|field|runnable
specifier|private
specifier|final
name|Runnable
name|runnable
decl_stmt|;
DECL|field|executor
specifier|private
specifier|final
name|Executor
name|executor
decl_stmt|;
DECL|method|ThreadedRunnable
name|ThreadedRunnable
parameter_list|(
name|Runnable
name|runnable
parameter_list|,
name|Executor
name|executor
parameter_list|)
block|{
name|this
operator|.
name|runnable
operator|=
name|runnable
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|executor
expr_stmt|;
block|}
DECL|method|run
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|executor
operator|.
name|execute
argument_list|(
name|runnable
argument_list|)
expr_stmt|;
block|}
DECL|method|hashCode
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|runnable
operator|.
name|hashCode
argument_list|()
return|;
block|}
DECL|method|equals
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
name|runnable
operator|.
name|equals
argument_list|(
name|obj
argument_list|)
return|;
block|}
DECL|method|toString
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"[threaded] "
operator|+
name|runnable
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

