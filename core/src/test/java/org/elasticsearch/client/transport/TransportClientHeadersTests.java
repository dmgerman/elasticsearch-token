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
name|Version
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
name|liveness
operator|.
name|LivenessResponse
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
name|liveness
operator|.
name|TransportLivenessAction
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
name|state
operator|.
name|ClusterStateAction
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
name|state
operator|.
name|ClusterStateResponse
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
name|AbstractClientHeadersTestCase
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
name|ClusterState
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
name|cluster
operator|.
name|node
operator|.
name|DiscoveryNodes
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
name|TransportAddress
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|Environment
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|NetworkPlugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|PluginsService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|transport
operator|.
name|MockTransportService
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
name|MockTransportClient
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
name|TransportInterceptor
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
name|TransportRequest
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
name|TransportRequestHandler
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
name|TransportRequestOptions
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
name|TransportResponse
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
name|TransportResponseHandler
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|CountDownLatch
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
name|TimeUnit
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
name|is
import|;
end_import

begin_class
DECL|class|TransportClientHeadersTests
specifier|public
class|class
name|TransportClientHeadersTests
extends|extends
name|AbstractClientHeadersTestCase
block|{
DECL|field|transportService
specifier|private
name|MockTransportService
name|transportService
decl_stmt|;
annotation|@
name|Override
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|super
operator|.
name|tearDown
argument_list|()
expr_stmt|;
name|transportService
operator|.
name|stop
argument_list|()
expr_stmt|;
name|transportService
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
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
name|transportService
operator|=
name|MockTransportService
operator|.
name|local
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|,
name|threadPool
argument_list|)
expr_stmt|;
name|transportService
operator|.
name|start
argument_list|()
expr_stmt|;
name|transportService
operator|.
name|acceptIncomingRequests
argument_list|()
expr_stmt|;
name|TransportClient
name|client
init|=
operator|new
name|MockTransportClient
argument_list|(
name|Settings
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
literal|"cluster.name"
argument_list|,
literal|"cluster1"
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.name"
argument_list|,
literal|"transport_client_"
operator|+
name|this
operator|.
name|getTestName
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|headersSettings
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|InternalTransportServiceInterceptor
operator|.
name|TestPlugin
operator|.
name|class
argument_list|)
decl_stmt|;
name|InternalTransportServiceInterceptor
operator|.
name|TestPlugin
name|plugin
init|=
name|client
operator|.
name|injector
operator|.
name|getInstance
argument_list|(
name|PluginsService
operator|.
name|class
argument_list|)
operator|.
name|filterPlugins
argument_list|(
name|InternalTransportServiceInterceptor
operator|.
name|TestPlugin
operator|.
name|class
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
name|findFirst
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|plugin
operator|.
name|instance
operator|.
name|threadPool
operator|=
name|client
operator|.
name|threadPool
argument_list|()
expr_stmt|;
name|plugin
operator|.
name|instance
operator|.
name|address
operator|=
name|transportService
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
expr_stmt|;
name|client
operator|.
name|addTransportAddress
argument_list|(
name|transportService
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|client
return|;
block|}
DECL|method|testWithSniffing
specifier|public
name|void
name|testWithSniffing
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|TransportClient
name|client
init|=
operator|new
name|MockTransportClient
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"client.transport.sniff"
argument_list|,
literal|true
argument_list|)
operator|.
name|put
argument_list|(
literal|"cluster.name"
argument_list|,
literal|"cluster1"
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.name"
argument_list|,
literal|"transport_client_"
operator|+
name|this
operator|.
name|getTestName
argument_list|()
operator|+
literal|"_1"
argument_list|)
operator|.
name|put
argument_list|(
literal|"client.transport.nodes_sampler_interval"
argument_list|,
literal|"1s"
argument_list|)
operator|.
name|put
argument_list|(
name|HEADER_SETTINGS
argument_list|)
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|InternalTransportServiceInterceptor
operator|.
name|TestPlugin
operator|.
name|class
argument_list|)
init|)
block|{
name|InternalTransportServiceInterceptor
operator|.
name|TestPlugin
name|plugin
init|=
name|client
operator|.
name|injector
operator|.
name|getInstance
argument_list|(
name|PluginsService
operator|.
name|class
argument_list|)
operator|.
name|filterPlugins
argument_list|(
name|InternalTransportServiceInterceptor
operator|.
name|TestPlugin
operator|.
name|class
argument_list|)
operator|.
name|stream
argument_list|()
operator|.
name|findFirst
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|plugin
operator|.
name|instance
operator|.
name|threadPool
operator|=
name|client
operator|.
name|threadPool
argument_list|()
expr_stmt|;
name|plugin
operator|.
name|instance
operator|.
name|address
operator|=
name|transportService
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
expr_stmt|;
name|client
operator|.
name|addTransportAddress
argument_list|(
name|transportService
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|plugin
operator|.
name|instance
operator|.
name|clusterStateLatch
operator|.
name|await
argument_list|(
literal|5
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
condition|)
block|{
name|fail
argument_list|(
literal|"takes way too long to get the cluster state"
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|client
operator|.
name|connectedNodes
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
operator|.
name|connectedNodes
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getAddress
argument_list|()
argument_list|,
name|is
argument_list|(
name|transportService
operator|.
name|boundAddress
argument_list|()
operator|.
name|publishAddress
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|InternalTransportServiceInterceptor
specifier|public
specifier|static
class|class
name|InternalTransportServiceInterceptor
implements|implements
name|TransportInterceptor
block|{
DECL|field|threadPool
name|ThreadPool
name|threadPool
decl_stmt|;
DECL|field|address
name|TransportAddress
name|address
decl_stmt|;
DECL|class|TestPlugin
specifier|public
specifier|static
class|class
name|TestPlugin
extends|extends
name|Plugin
implements|implements
name|NetworkPlugin
block|{
DECL|field|instance
specifier|private
name|InternalTransportServiceInterceptor
name|instance
init|=
operator|new
name|InternalTransportServiceInterceptor
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|getTransportInterceptors
specifier|public
name|List
argument_list|<
name|TransportInterceptor
argument_list|>
name|getTransportInterceptors
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singletonList
argument_list|(
operator|new
name|TransportInterceptor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
extends|extends
name|TransportRequest
parameter_list|>
name|TransportRequestHandler
argument_list|<
name|T
argument_list|>
name|interceptHandler
parameter_list|(
name|String
name|action
parameter_list|,
name|TransportRequestHandler
argument_list|<
name|T
argument_list|>
name|actualHandler
parameter_list|)
block|{
return|return
name|instance
operator|.
name|interceptHandler
argument_list|(
name|action
argument_list|,
name|actualHandler
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncSender
name|interceptSender
parameter_list|(
name|AsyncSender
name|sender
parameter_list|)
block|{
return|return
name|instance
operator|.
name|interceptSender
argument_list|(
name|sender
argument_list|)
return|;
block|}
block|}
argument_list|)
return|;
block|}
block|}
DECL|field|clusterStateLatch
specifier|final
name|CountDownLatch
name|clusterStateLatch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|interceptSender
specifier|public
name|AsyncSender
name|interceptSender
parameter_list|(
name|AsyncSender
name|sender
parameter_list|)
block|{
return|return
operator|new
name|AsyncSender
argument_list|()
block|{
annotation|@
name|Override
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
name|TransportLivenessAction
operator|.
name|NAME
operator|.
name|equals
argument_list|(
name|action
argument_list|)
condition|)
block|{
name|assertHeaders
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
operator|(
operator|(
name|TransportResponseHandler
argument_list|<
name|LivenessResponse
argument_list|>
operator|)
name|handler
operator|)
operator|.
name|handleResponse
argument_list|(
operator|new
name|LivenessResponse
argument_list|(
operator|new
name|ClusterName
argument_list|(
literal|"cluster1"
argument_list|)
argument_list|,
name|node
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
if|if
condition|(
name|ClusterStateAction
operator|.
name|NAME
operator|.
name|equals
argument_list|(
name|action
argument_list|)
condition|)
block|{
name|assertHeaders
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
name|ClusterName
name|cluster1
init|=
operator|new
name|ClusterName
argument_list|(
literal|"cluster1"
argument_list|)
decl_stmt|;
name|ClusterState
operator|.
name|Builder
name|builder
init|=
name|ClusterState
operator|.
name|builder
argument_list|(
name|cluster1
argument_list|)
decl_stmt|;
comment|//the sniffer detects only data nodes
name|builder
operator|.
name|nodes
argument_list|(
name|DiscoveryNodes
operator|.
name|builder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|DiscoveryNode
argument_list|(
literal|"node_id"
argument_list|,
name|address
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
name|singleton
argument_list|(
name|DiscoveryNode
operator|.
name|Role
operator|.
name|DATA
argument_list|)
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
operator|(
operator|(
name|TransportResponseHandler
argument_list|<
name|ClusterStateResponse
argument_list|>
operator|)
name|handler
operator|)
operator|.
name|handleResponse
argument_list|(
operator|new
name|ClusterStateResponse
argument_list|(
name|cluster1
argument_list|,
name|builder
operator|.
name|build
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|clusterStateLatch
operator|.
name|countDown
argument_list|()
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
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
block|}
block|}
end_class

end_unit

