begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.component
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|component
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
name|settings
operator|.
name|Settings
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AbstractLifecycleComponent
specifier|public
specifier|abstract
class|class
name|AbstractLifecycleComponent
parameter_list|<
name|T
parameter_list|>
extends|extends
name|AbstractComponent
implements|implements
name|LifecycleComponent
argument_list|<
name|T
argument_list|>
block|{
DECL|field|lifecycle
specifier|protected
specifier|final
name|Lifecycle
name|lifecycle
init|=
operator|new
name|Lifecycle
argument_list|()
decl_stmt|;
DECL|field|listeners
specifier|private
specifier|final
name|List
argument_list|<
name|LifecycleListener
argument_list|>
name|listeners
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|AbstractLifecycleComponent
specifier|protected
name|AbstractLifecycleComponent
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
block|}
DECL|method|AbstractLifecycleComponent
specifier|protected
name|AbstractLifecycleComponent
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Class
name|customClass
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|customClass
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|lifecycleState
specifier|public
name|Lifecycle
operator|.
name|State
name|lifecycleState
parameter_list|()
block|{
return|return
name|this
operator|.
name|lifecycle
operator|.
name|state
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|addLifecycleListener
specifier|public
name|void
name|addLifecycleListener
parameter_list|(
name|LifecycleListener
name|listener
parameter_list|)
block|{
name|listeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|removeLifecycleListener
specifier|public
name|void
name|removeLifecycleListener
parameter_list|(
name|LifecycleListener
name|listener
parameter_list|)
block|{
name|listeners
operator|.
name|remove
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
annotation|@
name|Override
DECL|method|start
specifier|public
name|T
name|start
parameter_list|()
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|canMoveToStarted
argument_list|()
condition|)
block|{
return|return
operator|(
name|T
operator|)
name|this
return|;
block|}
for|for
control|(
name|LifecycleListener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|beforeStart
argument_list|()
expr_stmt|;
block|}
name|doStart
argument_list|()
expr_stmt|;
name|lifecycle
operator|.
name|moveToStarted
argument_list|()
expr_stmt|;
for|for
control|(
name|LifecycleListener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|afterStart
argument_list|()
expr_stmt|;
block|}
return|return
operator|(
name|T
operator|)
name|this
return|;
block|}
DECL|method|doStart
specifier|protected
specifier|abstract
name|void
name|doStart
parameter_list|()
function_decl|;
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"unchecked"
block|}
argument_list|)
annotation|@
name|Override
DECL|method|stop
specifier|public
name|T
name|stop
parameter_list|()
block|{
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|canMoveToStopped
argument_list|()
condition|)
block|{
return|return
operator|(
name|T
operator|)
name|this
return|;
block|}
for|for
control|(
name|LifecycleListener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|beforeStop
argument_list|()
expr_stmt|;
block|}
name|lifecycle
operator|.
name|moveToStopped
argument_list|()
expr_stmt|;
name|doStop
argument_list|()
expr_stmt|;
for|for
control|(
name|LifecycleListener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|afterStop
argument_list|()
expr_stmt|;
block|}
return|return
operator|(
name|T
operator|)
name|this
return|;
block|}
DECL|method|doStop
specifier|protected
specifier|abstract
name|void
name|doStop
parameter_list|()
function_decl|;
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|lifecycle
operator|.
name|started
argument_list|()
condition|)
block|{
name|stop
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
operator|!
name|lifecycle
operator|.
name|canMoveToClosed
argument_list|()
condition|)
block|{
return|return;
block|}
for|for
control|(
name|LifecycleListener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|beforeClose
argument_list|()
expr_stmt|;
block|}
name|lifecycle
operator|.
name|moveToClosed
argument_list|()
expr_stmt|;
name|doClose
argument_list|()
expr_stmt|;
for|for
control|(
name|LifecycleListener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|afterClose
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|doClose
specifier|protected
specifier|abstract
name|void
name|doClose
parameter_list|()
function_decl|;
block|}
end_class

end_unit

