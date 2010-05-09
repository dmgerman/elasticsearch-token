begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.jmx.action
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|jmx
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
name|util
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
name|ElasticSearchException
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
name|ClusterService
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
name|jmx
operator|.
name|JmxService
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
name|BaseTransportRequestHandler
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
name|FutureTransportResponseHandler
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
name|TransportChannel
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
name|io
operator|.
name|stream
operator|.
name|StringStreamable
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
name|io
operator|.
name|stream
operator|.
name|VoidStreamable
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

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|GetJmxServiceUrlAction
specifier|public
class|class
name|GetJmxServiceUrlAction
extends|extends
name|AbstractComponent
block|{
DECL|field|jmxService
specifier|private
specifier|final
name|JmxService
name|jmxService
decl_stmt|;
DECL|field|transportService
specifier|private
specifier|final
name|TransportService
name|transportService
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|method|GetJmxServiceUrlAction
annotation|@
name|Inject
specifier|public
name|GetJmxServiceUrlAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|JmxService
name|jmxService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|jmxService
operator|=
name|jmxService
expr_stmt|;
name|this
operator|.
name|transportService
operator|=
name|transportService
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|transportService
operator|.
name|registerHandler
argument_list|(
name|GetJmxServiceUrlTransportHandler
operator|.
name|ACTION
argument_list|,
operator|new
name|GetJmxServiceUrlTransportHandler
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|obtainPublishUrl
specifier|public
name|String
name|obtainPublishUrl
parameter_list|(
specifier|final
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
if|if
condition|(
name|clusterService
operator|.
name|state
argument_list|()
operator|.
name|nodes
argument_list|()
operator|.
name|localNodeId
argument_list|()
operator|.
name|equals
argument_list|(
name|node
operator|.
name|id
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|jmxService
operator|.
name|publishUrl
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|transportService
operator|.
name|submitRequest
argument_list|(
name|node
argument_list|,
name|GetJmxServiceUrlTransportHandler
operator|.
name|ACTION
argument_list|,
name|VoidStreamable
operator|.
name|INSTANCE
argument_list|,
operator|new
name|FutureTransportResponseHandler
argument_list|<
name|StringStreamable
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|StringStreamable
name|newInstance
parameter_list|()
block|{
return|return
operator|new
name|StringStreamable
argument_list|()
return|;
block|}
block|}
argument_list|)
operator|.
name|txGet
argument_list|()
operator|.
name|get
argument_list|()
return|;
block|}
block|}
DECL|class|GetJmxServiceUrlTransportHandler
specifier|private
class|class
name|GetJmxServiceUrlTransportHandler
extends|extends
name|BaseTransportRequestHandler
argument_list|<
name|VoidStreamable
argument_list|>
block|{
DECL|field|ACTION
specifier|static
specifier|final
name|String
name|ACTION
init|=
literal|"jmx/publishUrl"
decl_stmt|;
DECL|method|newInstance
annotation|@
name|Override
specifier|public
name|VoidStreamable
name|newInstance
parameter_list|()
block|{
return|return
name|VoidStreamable
operator|.
name|INSTANCE
return|;
block|}
DECL|method|messageReceived
annotation|@
name|Override
specifier|public
name|void
name|messageReceived
parameter_list|(
name|VoidStreamable
name|request
parameter_list|,
name|TransportChannel
name|channel
parameter_list|)
throws|throws
name|Exception
block|{
name|channel
operator|.
name|sendResponse
argument_list|(
operator|new
name|StringStreamable
argument_list|(
name|jmxService
operator|.
name|publishUrl
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

