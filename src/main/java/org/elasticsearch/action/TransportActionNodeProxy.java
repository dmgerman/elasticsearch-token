begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
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
name|support
operator|.
name|PlainActionFuture
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNode
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
name|transport
operator|.
name|BaseTransportResponseHandler
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
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
comment|/**  * A generic proxy that will execute the given action against a specific node.  */
end_comment

begin_class
DECL|class|TransportActionNodeProxy
specifier|public
class|class
name|TransportActionNodeProxy
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
block|{
DECL|field|transportService
specifier|protected
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|action
specifier|private
specifier|final
name|GenericAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|action
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportActionNodeProxy
specifier|public
name|TransportActionNodeProxy
parameter_list|(
name|GenericAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|action
parameter_list|,
name|TransportService
name|transportService
parameter_list|)
block|{
name|this
operator|.
name|action
operator|=
name|action
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
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
name|DiscoveryNode
name|node
parameter_list|,
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
name|request
operator|.
name|listenerThreaded
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|execute
argument_list|(
name|node
argument_list|,
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
name|DiscoveryNode
name|node
parameter_list|,
specifier|final
name|Request
name|request
parameter_list|,
specifier|final
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
block|{
name|transportService
operator|.
name|sendRequest
argument_list|(
name|node
argument_list|,
name|action
operator|.
name|name
argument_list|()
argument_list|,
name|request
argument_list|,
name|action
operator|.
name|options
argument_list|()
argument_list|,
operator|new
name|BaseTransportResponseHandler
argument_list|<
name|Response
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Response
name|newInstance
parameter_list|()
block|{
return|return
name|action
operator|.
name|newResponse
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|executor
parameter_list|()
block|{
if|if
condition|(
name|request
operator|.
name|listenerThreaded
argument_list|()
condition|)
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|GENERIC
return|;
block|}
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|SAME
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleResponse
parameter_list|(
name|Response
name|response
parameter_list|)
block|{
name|listener
operator|.
name|onResponse
argument_list|(
name|response
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|handleException
parameter_list|(
name|TransportException
name|exp
parameter_list|)
block|{
name|listener
operator|.
name|onFailure
argument_list|(
name|exp
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

