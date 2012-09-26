begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.transport.support
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|transport
operator|.
name|support
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

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
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|IndicesAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|IndicesAdminClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|support
operator|.
name|AbstractIndicesAdminClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|transport
operator|.
name|TransportClientNodesService
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
name|collect
operator|.
name|MapBuilder
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
name|TransportService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|class|InternalTransportIndicesAdminClient
specifier|public
class|class
name|InternalTransportIndicesAdminClient
extends|extends
name|AbstractIndicesAdminClient
implements|implements
name|IndicesAdminClient
block|{
DECL|field|nodesService
specifier|private
specifier|final
name|TransportClientNodesService
name|nodesService
decl_stmt|;
DECL|field|threadPool
specifier|private
specifier|final
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|actions
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|IndicesAction
argument_list|,
name|TransportActionNodeProxy
argument_list|>
name|actions
decl_stmt|;
annotation|@
name|Inject
DECL|method|InternalTransportIndicesAdminClient
specifier|public
name|InternalTransportIndicesAdminClient
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|TransportClientNodesService
name|nodesService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|GenericAction
argument_list|>
name|actions
parameter_list|)
block|{
name|this
operator|.
name|nodesService
operator|=
name|nodesService
expr_stmt|;
name|this
operator|.
name|threadPool
operator|=
name|threadPool
expr_stmt|;
name|MapBuilder
argument_list|<
name|IndicesAction
argument_list|,
name|TransportActionNodeProxy
argument_list|>
name|actionsBuilder
init|=
operator|new
name|MapBuilder
argument_list|<
name|IndicesAction
argument_list|,
name|TransportActionNodeProxy
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|GenericAction
name|action
range|:
name|actions
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|action
operator|instanceof
name|IndicesAction
condition|)
block|{
name|actionsBuilder
operator|.
name|put
argument_list|(
operator|(
name|IndicesAction
operator|)
name|action
argument_list|,
operator|new
name|TransportActionNodeProxy
argument_list|(
name|settings
argument_list|,
name|action
argument_list|,
name|transportService
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|actions
operator|=
name|actionsBuilder
operator|.
name|immutableMap
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|threadPool
specifier|public
name|ThreadPool
name|threadPool
parameter_list|()
block|{
return|return
name|this
operator|.
name|threadPool
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
DECL|method|execute
specifier|public
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|,
name|RequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|,
name|RequestBuilder
argument_list|>
parameter_list|>
name|ActionFuture
argument_list|<
name|Response
argument_list|>
name|execute
parameter_list|(
specifier|final
name|IndicesAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|,
name|RequestBuilder
argument_list|>
name|action
parameter_list|,
specifier|final
name|Request
name|request
parameter_list|)
block|{
specifier|final
name|TransportActionNodeProxy
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|proxy
init|=
name|actions
operator|.
name|get
argument_list|(
name|action
argument_list|)
decl_stmt|;
return|return
name|nodesService
operator|.
name|execute
argument_list|(
operator|new
name|TransportClientNodesService
operator|.
name|NodeCallback
argument_list|<
name|ActionFuture
argument_list|<
name|Response
argument_list|>
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|ActionFuture
argument_list|<
name|Response
argument_list|>
name|doWithNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
return|return
name|proxy
operator|.
name|execute
argument_list|(
name|node
argument_list|,
name|request
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
annotation|@
name|Override
DECL|method|execute
specifier|public
parameter_list|<
name|Request
extends|extends
name|ActionRequest
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|,
name|RequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|,
name|RequestBuilder
argument_list|>
parameter_list|>
name|void
name|execute
parameter_list|(
specifier|final
name|IndicesAction
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|,
name|RequestBuilder
argument_list|>
name|action
parameter_list|,
specifier|final
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
specifier|final
name|TransportActionNodeProxy
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|proxy
init|=
name|actions
operator|.
name|get
argument_list|(
name|action
argument_list|)
decl_stmt|;
name|nodesService
operator|.
name|execute
argument_list|(
operator|new
name|TransportClientNodesService
operator|.
name|NodeListenerCallback
argument_list|<
name|Response
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|doWithNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|proxy
operator|.
name|execute
argument_list|(
name|node
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

