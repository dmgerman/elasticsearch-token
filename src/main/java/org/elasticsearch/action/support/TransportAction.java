begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
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
name|action
operator|.
name|*
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
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|PlainActionFuture
operator|.
name|newFuture
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportAction
specifier|public
specifier|abstract
class|class
name|TransportAction
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
extends|extends
name|AbstractComponent
block|{
DECL|field|threadPool
specifier|protected
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|method|TransportAction
specifier|protected
name|TransportAction
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
block|}
DECL|method|execute
specifier|public
name|ActionFuture
argument_list|<
name|Response
argument_list|>
name|execute
parameter_list|(
name|Request
name|request
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|PlainActionFuture
argument_list|<
name|Response
argument_list|>
name|future
init|=
name|newFuture
argument_list|()
decl_stmt|;
comment|// since we don't have a listener, and we release a possible lock with the future
comment|// there is no need to execute it under a listener thread
name|request
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|execute
argument_list|(
name|request
argument_list|,
name|future
argument_list|)
expr_stmt|;
return|return
name|future
return|;
block|}
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|listenerThreaded
argument_list|()
condition|)
block|{
name|listener
operator|=
operator|new
name|ThreadedActionListener
argument_list|<
name|Response
argument_list|>
argument_list|(
name|threadPool
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
name|ActionRequestValidationException
name|validationException
init|=
name|request
operator|.
name|validate
argument_list|()
decl_stmt|;
if|if
condition|(
name|validationException
operator|!=
literal|null
condition|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|validationException
argument_list|)
expr_stmt|;
return|return;
block|}
try|try
block|{
name|doExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
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
name|trace
argument_list|(
literal|"Error during transport action execution."
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|doExecute
specifier|protected
specifier|abstract
name|void
name|doExecute
parameter_list|(
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
function_decl|;
DECL|class|ThreadedActionListener
specifier|static
class|class
name|ThreadedActionListener
parameter_list|<
name|Response
parameter_list|>
implements|implements
name|ActionListener
argument_list|<
name|Response
argument_list|>
block|{
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|listener
specifier|private
specifier|final
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
decl_stmt|;
DECL|method|ThreadedActionListener
name|ThreadedActionListener
parameter_list|(
name|ThreadPool
name|threadPool
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onResponse
specifier|public
name|void
name|onResponse
parameter_list|(
specifier|final
name|Response
name|response
parameter_list|)
block|{
name|threadPool
operator|.
name|generic
argument_list|()
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
name|void
name|onFailure
parameter_list|(
specifier|final
name|Throwable
name|e
parameter_list|)
block|{
name|threadPool
operator|.
name|generic
argument_list|()
operator|.
name|execute
argument_list|(
operator|new
name|Runnable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

