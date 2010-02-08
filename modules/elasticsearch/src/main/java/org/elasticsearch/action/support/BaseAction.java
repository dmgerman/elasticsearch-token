begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|util
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
name|Settings
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
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|BaseAction
specifier|public
specifier|abstract
class|class
name|BaseAction
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
implements|implements
name|Action
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
block|{
DECL|method|BaseAction
specifier|protected
name|BaseAction
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
DECL|method|submit
annotation|@
name|Override
specifier|public
name|ActionFuture
argument_list|<
name|Response
argument_list|>
name|submit
parameter_list|(
name|Request
name|request
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
return|return
name|submit
argument_list|(
name|request
argument_list|,
literal|null
argument_list|)
return|;
block|}
DECL|method|submit
annotation|@
name|Override
specifier|public
name|ActionFuture
argument_list|<
name|Response
argument_list|>
name|submit
parameter_list|(
name|Request
name|request
parameter_list|,
annotation|@
name|Nullable
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
name|PlainActionFuture
argument_list|<
name|Response
argument_list|>
name|future
init|=
name|newFuture
argument_list|(
name|listener
argument_list|)
decl_stmt|;
if|if
condition|(
name|listener
operator|==
literal|null
condition|)
block|{
comment|// since we don't have a listener, and we release a possible lock with the future
comment|// there is no need to execute it under a listener thread
name|request
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
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
annotation|@
name|Override
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
name|doExecute
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
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
name|responseActionListener
parameter_list|)
function_decl|;
block|}
end_class

end_unit

