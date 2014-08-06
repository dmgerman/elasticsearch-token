begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|transport
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|GenericAction
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
name|cluster
operator|.
name|node
operator|.
name|info
operator|.
name|NodeInfo
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
name|cluster
operator|.
name|node
operator|.
name|info
operator|.
name|NodesInfoAction
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
name|cluster
operator|.
name|node
operator|.
name|info
operator|.
name|NodesInfoResponse
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
name|AbstractClientHeadersTests
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
name|Client
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
name|ClusterName
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
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
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
name|transport
operator|.
name|LocalTransportAddress
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
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TransportClientHeadersTests
specifier|public
class|class
name|TransportClientHeadersTests
extends|extends
name|AbstractClientHeadersTests
block|{
DECL|field|address
specifier|private
specifier|static
specifier|final
name|LocalTransportAddress
name|address
init|=
operator|new
name|LocalTransportAddress
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|buildClient
specifier|protected
name|Client
name|buildClient
parameter_list|(
name|Settings
name|headersSettings
parameter_list|,
name|GenericAction
index|[]
name|testedActions
parameter_list|)
block|{
name|TransportClient
name|client
init|=
operator|new
name|TransportClient
argument_list|(
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"client.transport.sniff"
argument_list|,
literal|false
argument_list|)
operator|.
name|put
argument_list|(
name|TransportModule
operator|.
name|TRANSPORT_SERVICE_TYPE_KEY
argument_list|,
name|InternalTransportService
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|HEADER_SETTINGS
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
decl_stmt|;
name|client
operator|.
name|addTransportAddress
argument_list|(
name|address
argument_list|)
expr_stmt|;
return|return
name|client
return|;
block|}
DECL|class|InternalTransportService
specifier|public
specifier|static
class|class
name|InternalTransportService
extends|extends
name|TransportService
block|{
annotation|@
name|Inject
DECL|method|InternalTransportService
specifier|public
name|InternalTransportService
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|Transport
name|transport
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|transport
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|sendRequest
specifier|public
parameter_list|<
name|T
extends|extends
name|TransportResponse
parameter_list|>
name|void
name|sendRequest
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|,
name|String
name|action
parameter_list|,
name|TransportRequest
name|request
parameter_list|,
name|TransportRequestOptions
name|options
parameter_list|,
name|TransportResponseHandler
argument_list|<
name|T
argument_list|>
name|handler
parameter_list|)
block|{
if|if
condition|(
name|NodesInfoAction
operator|.
name|NAME
operator|.
name|equals
argument_list|(
name|action
argument_list|)
condition|)
block|{
operator|(
operator|(
name|TransportResponseHandler
argument_list|<
name|NodesInfoResponse
argument_list|>
operator|)
name|handler
operator|)
operator|.
name|handleResponse
argument_list|(
operator|new
name|NodesInfoResponse
argument_list|(
name|ClusterName
operator|.
name|DEFAULT
argument_list|,
operator|new
name|NodeInfo
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|handler
operator|.
name|handleException
argument_list|(
operator|new
name|TransportException
argument_list|(
literal|""
argument_list|,
operator|new
name|InternalException
argument_list|(
name|action
argument_list|,
name|request
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|nodeConnected
specifier|public
name|boolean
name|nodeConnected
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
block|{
name|assertThat
argument_list|(
operator|(
name|LocalTransportAddress
operator|)
name|node
operator|.
name|getAddress
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|address
argument_list|)
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|connectToNode
specifier|public
name|void
name|connectToNode
parameter_list|(
name|DiscoveryNode
name|node
parameter_list|)
throws|throws
name|ConnectTransportException
block|{
name|assertThat
argument_list|(
operator|(
name|LocalTransportAddress
operator|)
name|node
operator|.
name|getAddress
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|address
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

