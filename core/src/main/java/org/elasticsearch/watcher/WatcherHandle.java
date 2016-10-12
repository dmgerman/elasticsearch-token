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

begin_class
DECL|class|WatcherHandle
specifier|public
class|class
name|WatcherHandle
parameter_list|<
name|W
extends|extends
name|ResourceWatcher
parameter_list|>
block|{
DECL|field|monitor
specifier|private
specifier|final
name|ResourceWatcherService
operator|.
name|ResourceMonitor
name|monitor
decl_stmt|;
DECL|field|watcher
specifier|private
specifier|final
name|W
name|watcher
decl_stmt|;
DECL|method|WatcherHandle
name|WatcherHandle
parameter_list|(
name|ResourceWatcherService
operator|.
name|ResourceMonitor
name|monitor
parameter_list|,
name|W
name|watcher
parameter_list|)
block|{
name|this
operator|.
name|monitor
operator|=
name|monitor
expr_stmt|;
name|this
operator|.
name|watcher
operator|=
name|watcher
expr_stmt|;
block|}
DECL|method|watcher
specifier|public
name|W
name|watcher
parameter_list|()
block|{
return|return
name|watcher
return|;
block|}
DECL|method|frequency
specifier|public
name|ResourceWatcherService
operator|.
name|Frequency
name|frequency
parameter_list|()
block|{
return|return
name|monitor
operator|.
name|frequency
return|;
block|}
DECL|method|stop
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|monitor
operator|.
name|watchers
operator|.
name|remove
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
block|}
DECL|method|resume
specifier|public
name|void
name|resume
parameter_list|()
block|{
name|monitor
operator|.
name|watchers
operator|.
name|add
argument_list|(
name|watcher
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

