begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.threadpool.scaling
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|scaling
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
name|TransferThreadPoolExecutor
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
name|support
operator|.
name|AbstractThreadPool
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
name|Executors
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
name|Builder
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
DECL|class|ScalingThreadPool
specifier|public
class|class
name|ScalingThreadPool
extends|extends
name|AbstractThreadPool
block|{
DECL|field|min
specifier|final
name|int
name|min
decl_stmt|;
DECL|field|max
specifier|final
name|int
name|max
decl_stmt|;
DECL|field|keepAlive
specifier|final
name|TimeValue
name|keepAlive
decl_stmt|;
DECL|field|scheduledSize
specifier|final
name|int
name|scheduledSize
decl_stmt|;
DECL|method|ScalingThreadPool
specifier|public
name|ScalingThreadPool
parameter_list|()
block|{
name|this
argument_list|(
name|EMPTY_SETTINGS
argument_list|)
expr_stmt|;
block|}
DECL|method|ScalingThreadPool
annotation|@
name|Inject
specifier|public
name|ScalingThreadPool
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
name|this
operator|.
name|min
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"min"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|this
operator|.
name|max
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"max"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|this
operator|.
name|keepAlive
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"keep_alive"
argument_list|,
name|timeValueMinutes
argument_list|(
literal|60
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|scheduledSize
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"scheduled_size"
argument_list|,
literal|20
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Initializing {} thread pool with min[{}], max[{}], keep_alive[{}], scheduled_size[{}]"
argument_list|,
name|getType
argument_list|()
argument_list|,
name|min
argument_list|,
name|max
argument_list|,
name|keepAlive
argument_list|,
name|scheduledSize
argument_list|)
expr_stmt|;
name|scheduledExecutorService
operator|=
name|Executors
operator|.
name|newScheduledThreadPool
argument_list|(
name|scheduledSize
argument_list|,
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
literal|"[sc]"
argument_list|)
argument_list|)
expr_stmt|;
comment|//        executorService = TransferThreadPoolExecutor.newScalingExecutor(min, max, keepAlive.nanos(), TimeUnit.NANOSECONDS, EsExecutors.daemonThreadFactory(settings, "[tp]"));
name|executorService
operator|=
name|DynamicExecutors
operator|.
name|newScalingThreadPool
argument_list|(
name|min
argument_list|,
name|max
argument_list|,
name|keepAlive
operator|.
name|millis
argument_list|()
argument_list|,
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
literal|"[tp]"
argument_list|)
argument_list|)
expr_stmt|;
name|cached
operator|=
name|EsExecutors
operator|.
name|newCachedThreadPool
argument_list|(
name|keepAlive
argument_list|,
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
literal|"[cached]"
argument_list|)
argument_list|)
expr_stmt|;
name|started
operator|=
literal|true
expr_stmt|;
block|}
DECL|method|getMinThreads
annotation|@
name|Override
specifier|public
name|int
name|getMinThreads
parameter_list|()
block|{
return|return
name|min
return|;
block|}
DECL|method|getMaxThreads
annotation|@
name|Override
specifier|public
name|int
name|getMaxThreads
parameter_list|()
block|{
return|return
name|max
return|;
block|}
DECL|method|getSchedulerThreads
annotation|@
name|Override
specifier|public
name|int
name|getSchedulerThreads
parameter_list|()
block|{
return|return
name|scheduledSize
return|;
block|}
DECL|method|getPoolSize
annotation|@
name|Override
specifier|public
name|int
name|getPoolSize
parameter_list|()
block|{
if|if
condition|(
name|executorService
operator|instanceof
name|TransferThreadPoolExecutor
condition|)
block|{
return|return
operator|(
operator|(
name|TransferThreadPoolExecutor
operator|)
name|executorService
operator|)
operator|.
name|getPoolSize
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|executorService
operator|)
operator|.
name|getPoolSize
argument_list|()
return|;
block|}
block|}
DECL|method|getActiveCount
annotation|@
name|Override
specifier|public
name|int
name|getActiveCount
parameter_list|()
block|{
if|if
condition|(
name|executorService
operator|instanceof
name|TransferThreadPoolExecutor
condition|)
block|{
return|return
operator|(
operator|(
name|TransferThreadPoolExecutor
operator|)
name|executorService
operator|)
operator|.
name|getActiveCount
argument_list|()
return|;
block|}
else|else
block|{
return|return
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|executorService
operator|)
operator|.
name|getActiveCount
argument_list|()
return|;
block|}
block|}
DECL|method|getSchedulerPoolSize
annotation|@
name|Override
specifier|public
name|int
name|getSchedulerPoolSize
parameter_list|()
block|{
return|return
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|scheduledExecutorService
operator|)
operator|.
name|getPoolSize
argument_list|()
return|;
block|}
DECL|method|getSchedulerActiveCount
annotation|@
name|Override
specifier|public
name|int
name|getSchedulerActiveCount
parameter_list|()
block|{
return|return
operator|(
operator|(
name|ThreadPoolExecutor
operator|)
name|scheduledExecutorService
operator|)
operator|.
name|getActiveCount
argument_list|()
return|;
block|}
DECL|method|getType
annotation|@
name|Override
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
literal|"scaling"
return|;
block|}
block|}
end_class

end_unit

