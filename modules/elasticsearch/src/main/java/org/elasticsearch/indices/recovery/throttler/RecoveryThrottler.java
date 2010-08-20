begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.indices.recovery.throttler
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|recovery
operator|.
name|throttler
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
name|index
operator|.
name|shard
operator|.
name|ShardId
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|RecoveryThrottler
specifier|public
class|class
name|RecoveryThrottler
extends|extends
name|AbstractComponent
block|{
DECL|field|concurrentRecoveryMutex
specifier|private
specifier|final
name|Object
name|concurrentRecoveryMutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|field|concurrentRecoveries
specifier|private
specifier|final
name|int
name|concurrentRecoveries
decl_stmt|;
DECL|field|throttleInterval
specifier|private
specifier|final
name|TimeValue
name|throttleInterval
decl_stmt|;
DECL|field|onGoingRecoveries
specifier|private
specifier|volatile
name|int
name|onGoingRecoveries
init|=
literal|0
decl_stmt|;
DECL|field|concurrentStreams
specifier|private
specifier|final
name|int
name|concurrentStreams
decl_stmt|;
DECL|field|onGoingStreams
specifier|private
specifier|volatile
name|int
name|onGoingStreams
init|=
literal|0
decl_stmt|;
DECL|field|concurrentStreamsMutex
specifier|private
specifier|final
name|Object
name|concurrentStreamsMutex
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
DECL|method|RecoveryThrottler
annotation|@
name|Inject
specifier|public
name|RecoveryThrottler
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
name|int
name|defaultConcurrent
init|=
name|Runtime
operator|.
name|getRuntime
argument_list|()
operator|.
name|availableProcessors
argument_list|()
operator|+
literal|1
decl_stmt|;
comment|// tap it at 10 (is it a good number?)
if|if
condition|(
name|defaultConcurrent
operator|>
literal|10
condition|)
block|{
name|defaultConcurrent
operator|=
literal|10
expr_stmt|;
block|}
name|concurrentRecoveries
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"concurrent_recoveries"
argument_list|,
name|defaultConcurrent
argument_list|)
expr_stmt|;
name|concurrentStreams
operator|=
name|componentSettings
operator|.
name|getAsInt
argument_list|(
literal|"concurrent_streams"
argument_list|,
name|defaultConcurrent
argument_list|)
expr_stmt|;
name|throttleInterval
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"interval"
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"concurrent_recoveries [{}], concurrent_streams [{}] interval [{}]"
argument_list|,
name|concurrentRecoveries
argument_list|,
name|concurrentStreams
argument_list|,
name|throttleInterval
argument_list|)
expr_stmt|;
block|}
DECL|method|tryRecovery
specifier|public
name|boolean
name|tryRecovery
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
synchronized|synchronized
init|(
name|concurrentRecoveryMutex
init|)
block|{
if|if
condition|(
name|onGoingRecoveries
operator|+
literal|1
operator|>
name|concurrentRecoveries
condition|)
block|{
return|return
literal|false
return|;
block|}
name|onGoingRecoveries
operator|++
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"Recovery allowed for [{}], on going [{}], allowed [{}], reason [{}]"
argument_list|,
name|shardId
argument_list|,
name|onGoingRecoveries
argument_list|,
name|concurrentRecoveries
argument_list|,
name|reason
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
DECL|method|recoveryDone
specifier|public
name|void
name|recoveryDone
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|reason
parameter_list|)
block|{
synchronized|synchronized
init|(
name|concurrentRecoveryMutex
init|)
block|{
operator|--
name|onGoingRecoveries
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"Recovery done for [{}], on going [{}], allowed [{}], reason [{}]"
argument_list|,
name|shardId
argument_list|,
name|onGoingRecoveries
argument_list|,
name|concurrentRecoveries
argument_list|,
name|reason
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|onGoingRecoveries
specifier|public
name|int
name|onGoingRecoveries
parameter_list|()
block|{
return|return
name|onGoingRecoveries
return|;
block|}
DECL|method|tryStream
specifier|public
name|boolean
name|tryStream
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|streamName
parameter_list|)
block|{
synchronized|synchronized
init|(
name|concurrentStreamsMutex
init|)
block|{
if|if
condition|(
name|onGoingStreams
operator|+
literal|1
operator|>
name|concurrentStreams
condition|)
block|{
return|return
literal|false
return|;
block|}
name|onGoingStreams
operator|++
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"Stream [{}] allowed for [{}], on going [{}], allowed [{}]"
argument_list|,
name|streamName
argument_list|,
name|shardId
argument_list|,
name|onGoingStreams
argument_list|,
name|concurrentStreams
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
block|}
DECL|method|streamDone
specifier|public
name|void
name|streamDone
parameter_list|(
name|ShardId
name|shardId
parameter_list|,
name|String
name|streamName
parameter_list|)
block|{
synchronized|synchronized
init|(
name|concurrentStreamsMutex
init|)
block|{
operator|--
name|onGoingStreams
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"Stream [{}] done for [{}], on going [{}], allowed [{}]"
argument_list|,
name|streamName
argument_list|,
name|shardId
argument_list|,
name|onGoingStreams
argument_list|,
name|concurrentStreams
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|onGoingStreams
specifier|public
name|int
name|onGoingStreams
parameter_list|()
block|{
return|return
name|onGoingStreams
return|;
block|}
DECL|method|throttleInterval
specifier|public
name|TimeValue
name|throttleInterval
parameter_list|()
block|{
return|return
name|throttleInterval
return|;
block|}
block|}
end_class

end_unit

