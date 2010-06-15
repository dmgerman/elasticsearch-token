begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.monitor.jvm
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|jvm
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|ImmutableSet
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
name|AbstractLifecycleComponent
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
name|monitor
operator|.
name|dump
operator|.
name|DumpGenerator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|dump
operator|.
name|DumpMonitorService
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
name|HashSet
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
name|ScheduledFuture
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

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|dump
operator|.
name|summary
operator|.
name|SummaryDumpContributor
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
name|monitor
operator|.
name|dump
operator|.
name|thread
operator|.
name|ThreadDumpContributor
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
name|monitor
operator|.
name|jvm
operator|.
name|DeadlockAnalyzer
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
name|monitor
operator|.
name|jvm
operator|.
name|JvmStats
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|JvmMonitorService
specifier|public
class|class
name|JvmMonitorService
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|JvmMonitorService
argument_list|>
block|{
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|dumpMonitorService
specifier|private
specifier|final
name|DumpMonitorService
name|dumpMonitorService
decl_stmt|;
DECL|field|enabled
specifier|private
specifier|final
name|boolean
name|enabled
decl_stmt|;
DECL|field|interval
specifier|private
specifier|final
name|TimeValue
name|interval
decl_stmt|;
DECL|field|gcCollectionWarning
specifier|private
specifier|final
name|TimeValue
name|gcCollectionWarning
decl_stmt|;
DECL|field|scheduledFuture
specifier|private
specifier|volatile
name|ScheduledFuture
name|scheduledFuture
decl_stmt|;
DECL|method|JvmMonitorService
annotation|@
name|Inject
specifier|public
name|JvmMonitorService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|DumpMonitorService
name|dumpMonitorService
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
name|dumpMonitorService
operator|=
name|dumpMonitorService
expr_stmt|;
name|this
operator|.
name|enabled
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"enabled"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|this
operator|.
name|interval
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"interval"
argument_list|,
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|gcCollectionWarning
operator|=
name|componentSettings
operator|.
name|getAsTime
argument_list|(
literal|"gcCollectionWarning"
argument_list|,
name|timeValueSeconds
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|doStart
annotation|@
name|Override
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
return|return;
block|}
name|scheduledFuture
operator|=
name|threadPool
operator|.
name|scheduleWithFixedDelay
argument_list|(
operator|new
name|JvmMonitor
argument_list|()
argument_list|,
name|interval
argument_list|)
expr_stmt|;
block|}
DECL|method|doStop
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
return|return;
block|}
name|scheduledFuture
operator|.
name|cancel
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|doClose
annotation|@
name|Override
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticSearchException
block|{     }
DECL|class|JvmMonitor
specifier|private
class|class
name|JvmMonitor
implements|implements
name|Runnable
block|{
DECL|field|lastJvmStats
specifier|private
name|JvmStats
name|lastJvmStats
init|=
name|jvmStats
argument_list|()
decl_stmt|;
DECL|field|lastSeenDeadlocks
specifier|private
specifier|final
name|Set
argument_list|<
name|DeadlockAnalyzer
operator|.
name|Deadlock
argument_list|>
name|lastSeenDeadlocks
init|=
operator|new
name|HashSet
argument_list|<
name|DeadlockAnalyzer
operator|.
name|Deadlock
argument_list|>
argument_list|()
decl_stmt|;
DECL|method|JvmMonitor
specifier|public
name|JvmMonitor
parameter_list|()
block|{         }
DECL|method|run
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|monitorDeadlock
argument_list|()
expr_stmt|;
name|monitorLongGc
argument_list|()
expr_stmt|;
block|}
DECL|method|monitorLongGc
specifier|private
name|void
name|monitorLongGc
parameter_list|()
block|{
name|JvmStats
name|currentJvmStats
init|=
name|jvmStats
argument_list|()
decl_stmt|;
name|long
name|collectionTime
init|=
name|currentJvmStats
operator|.
name|gc
argument_list|()
operator|.
name|collectionTime
argument_list|()
operator|.
name|millis
argument_list|()
operator|-
name|lastJvmStats
operator|.
name|gc
argument_list|()
operator|.
name|collectionTime
argument_list|()
operator|.
name|millis
argument_list|()
decl_stmt|;
if|if
condition|(
name|collectionTime
operator|>
name|gcCollectionWarning
operator|.
name|millis
argument_list|()
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"Long GC collection occurred, took ["
operator|+
operator|new
name|TimeValue
argument_list|(
name|collectionTime
argument_list|)
operator|+
literal|"], breached threshold ["
operator|+
name|gcCollectionWarning
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|lastJvmStats
operator|=
name|currentJvmStats
expr_stmt|;
block|}
DECL|method|monitorDeadlock
specifier|private
name|void
name|monitorDeadlock
parameter_list|()
block|{
name|DeadlockAnalyzer
operator|.
name|Deadlock
index|[]
name|deadlocks
init|=
name|deadlockAnalyzer
argument_list|()
operator|.
name|findDeadlocks
argument_list|()
decl_stmt|;
if|if
condition|(
name|deadlocks
operator|!=
literal|null
operator|&&
name|deadlocks
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|ImmutableSet
argument_list|<
name|DeadlockAnalyzer
operator|.
name|Deadlock
argument_list|>
name|asSet
init|=
operator|new
name|ImmutableSet
operator|.
name|Builder
argument_list|<
name|DeadlockAnalyzer
operator|.
name|Deadlock
argument_list|>
argument_list|()
operator|.
name|add
argument_list|(
name|deadlocks
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|asSet
operator|.
name|equals
argument_list|(
name|lastSeenDeadlocks
argument_list|)
condition|)
block|{
name|DumpGenerator
operator|.
name|Result
name|genResult
init|=
name|dumpMonitorService
operator|.
name|generateDump
argument_list|(
literal|"deadlock"
argument_list|,
literal|null
argument_list|,
name|SUMMARY
argument_list|,
name|THREAD_DUMP
argument_list|)
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"Detected Deadlock(s)"
argument_list|)
decl_stmt|;
for|for
control|(
name|DeadlockAnalyzer
operator|.
name|Deadlock
name|deadlock
range|:
name|asSet
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"\n   ----> "
argument_list|)
operator|.
name|append
argument_list|(
name|deadlock
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"\nDump generated ["
argument_list|)
operator|.
name|append
argument_list|(
name|genResult
operator|.
name|location
argument_list|()
argument_list|)
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|error
argument_list|(
name|sb
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|lastSeenDeadlocks
operator|.
name|clear
argument_list|()
expr_stmt|;
name|lastSeenDeadlocks
operator|.
name|addAll
argument_list|(
name|asSet
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|lastSeenDeadlocks
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

