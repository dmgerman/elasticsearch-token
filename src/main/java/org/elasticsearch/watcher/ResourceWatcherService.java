begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.watcher
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|watcher
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|CopyOnWriteArrayList
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
name|timeValueSeconds
import|;
end_import

begin_comment
comment|/**  * Generic resource watcher service  *  * Other elasticsearch services can register their resource watchers with this service using {@link #add(ResourceWatcher)}  * method. This service will call {@link org.elasticsearch.watcher.ResourceWatcher#checkAndNotify()} method of all  * registered watcher periodically. The frequency of checks can be specified using {@code watcher.interval} setting, which  * defaults to {@code 60s}. The service can be disabled by setting {@code watcher.enabled} setting to {@code false}.  */
end_comment

begin_class
DECL|class|ResourceWatcherService
specifier|public
class|class
name|ResourceWatcherService
extends|extends
name|AbstractLifecycleComponent
argument_list|<
name|ResourceWatcherService
argument_list|>
block|{
DECL|field|watchers
specifier|private
specifier|final
name|List
argument_list|<
name|ResourceWatcher
argument_list|>
name|watchers
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|scheduledFuture
specifier|private
specifier|volatile
name|ScheduledFuture
name|scheduledFuture
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
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
annotation|@
name|Inject
DECL|method|ResourceWatcherService
specifier|public
name|ResourceWatcherService
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
literal|60
argument_list|)
argument_list|)
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStart
specifier|protected
name|void
name|doStart
parameter_list|()
throws|throws
name|ElasticsearchException
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
name|ResourceMonitor
argument_list|()
argument_list|,
name|interval
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doStop
specifier|protected
name|void
name|doStop
parameter_list|()
throws|throws
name|ElasticsearchException
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
annotation|@
name|Override
DECL|method|doClose
specifier|protected
name|void
name|doClose
parameter_list|()
throws|throws
name|ElasticsearchException
block|{     }
comment|/**      * Register new resource watcher      */
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|ResourceWatcher
name|watcher
parameter_list|)
block|{
name|watcher
operator|.
name|init
argument_list|()
expr_stmt|;
name|watchers
operator|.
name|add
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
block|}
comment|/**      * Unregister a resource watcher      */
DECL|method|remove
specifier|public
name|void
name|remove
parameter_list|(
name|ResourceWatcher
name|watcher
parameter_list|)
block|{
name|watchers
operator|.
name|remove
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
block|}
DECL|class|ResourceMonitor
specifier|private
class|class
name|ResourceMonitor
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
for|for
control|(
name|ResourceWatcher
name|watcher
range|:
name|watchers
control|)
block|{
name|watcher
operator|.
name|checkAndNotify
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

