begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.timer
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|timer
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
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
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
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
name|util
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
name|util
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
name|util
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
name|util
operator|.
name|timer
operator|.
name|HashedWheelTimer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|timer
operator|.
name|Timeout
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|timer
operator|.
name|Timer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|timer
operator|.
name|TimerTask
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
name|util
operator|.
name|TimeValue
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
name|util
operator|.
name|concurrent
operator|.
name|DynamicExecutors
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|TimerService
specifier|public
class|class
name|TimerService
extends|extends
name|AbstractComponent
block|{
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|timeEstimator
specifier|private
specifier|final
name|TimeEstimator
name|timeEstimator
decl_stmt|;
DECL|field|timeEstimatorFuture
specifier|private
specifier|final
name|ScheduledFuture
name|timeEstimatorFuture
decl_stmt|;
DECL|field|timer
specifier|private
specifier|final
name|Timer
name|timer
decl_stmt|;
DECL|field|tickDuration
specifier|private
specifier|final
name|TimeValue
name|tickDuration
decl_stmt|;
DECL|field|ticksPerWheel
specifier|private
specifier|final
name|int
name|ticksPerWheel
decl_stmt|;
DECL|method|TimerService
specifier|public
name|TimerService
parameter_list|(
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|this
argument_list|(
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
block|}
DECL|method|TimerService
annotation|@
name|Inject
specifier|public
name|TimerService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|timeEstimator
operator|=
operator|new
name|TimeEstimator
argument_list|()
expr_stmt|;
name|this
operator|.
name|timeEstimatorFuture
operator|=
name|threadPool
operator|.
name|scheduleWithFixedDelay
argument_list|(
name|timeEstimator
argument_list|,
literal|50
argument_list|,
literal|50
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
expr_stmt|;
name|this
operator|.
name|tickDuration
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"tick_duration"
argument_list|,
name|timeValueMillis
argument_list|(
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|ticksPerWheel
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"ticks_per_wheel"
argument_list|,
literal|1024
argument_list|)
expr_stmt|;
name|this
operator|.
name|timer
operator|=
operator|new
name|HashedWheelTimer
argument_list|(
name|logger
argument_list|,
name|daemonThreadFactory
argument_list|(
name|settings
argument_list|,
literal|"timer"
argument_list|)
argument_list|,
name|tickDuration
operator|.
name|millis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|,
name|ticksPerWheel
argument_list|)
expr_stmt|;
block|}
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|timeEstimatorFuture
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|timer
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
DECL|method|estimatedTimeInMillis
specifier|public
name|long
name|estimatedTimeInMillis
parameter_list|()
block|{
return|return
name|timeEstimator
operator|.
name|time
argument_list|()
return|;
block|}
DECL|method|newTimeout
specifier|public
name|Timeout
name|newTimeout
parameter_list|(
name|TimerTask
name|task
parameter_list|,
name|TimeValue
name|delay
parameter_list|)
block|{
return|return
name|newTimeout
argument_list|(
name|task
argument_list|,
name|delay
operator|.
name|nanos
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|NANOSECONDS
argument_list|)
return|;
block|}
DECL|method|newTimeout
specifier|public
name|Timeout
name|newTimeout
parameter_list|(
name|TimerTask
name|task
parameter_list|,
name|long
name|delay
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|timer
operator|.
name|newTimeout
argument_list|(
name|task
argument_list|,
name|delay
argument_list|,
name|unit
argument_list|)
return|;
block|}
DECL|class|TimeEstimator
specifier|private
specifier|static
class|class
name|TimeEstimator
implements|implements
name|Runnable
block|{
DECL|field|time
specifier|private
name|long
name|time
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
DECL|method|run
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|this
operator|.
name|time
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
DECL|method|time
specifier|public
name|long
name|time
parameter_list|()
block|{
return|return
name|this
operator|.
name|time
return|;
block|}
block|}
block|}
end_class

end_unit

