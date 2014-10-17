begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.transport
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
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
name|admin
operator|.
name|cluster
operator|.
name|health
operator|.
name|ClusterHealthResponse
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
name|health
operator|.
name|ClusterHealthStatus
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
name|TransportClient
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
name|InetSocketTransportAddress
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
name|ElasticsearchIntegrationTest
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
name|junit
operator|.
name|annotations
operator|.
name|Network
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
name|junit
operator|.
name|annotations
operator|.
name|TestLogging
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
name|TransportModule
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
name|netty
operator|.
name|NettyTransport
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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
name|settings
operator|.
name|ImmutableSettings
operator|.
name|settingsBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ElasticsearchIntegrationTest
operator|.
name|Scope
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

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|SUITE
argument_list|,
name|numDataNodes
operator|=
literal|1
argument_list|,
name|enableRandomBenchNodes
operator|=
literal|false
argument_list|)
DECL|class|NettyTransportMultiPortIntegrationTests
specifier|public
class|class
name|NettyTransportMultiPortIntegrationTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
DECL|field|randomPort
specifier|private
specifier|final
name|int
name|randomPort
init|=
name|randomIntBetween
argument_list|(
literal|1025
argument_list|,
literal|65000
argument_list|)
decl_stmt|;
DECL|field|randomPortRange
specifier|private
specifier|final
name|String
name|randomPortRange
init|=
name|String
operator|.
name|format
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|,
literal|"%s-%s"
argument_list|,
name|randomPort
argument_list|,
name|randomPort
operator|+
literal|10
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"network.host"
argument_list|,
literal|"127.0.0.1"
argument_list|)
operator|.
name|put
argument_list|(
name|TransportModule
operator|.
name|TRANSPORT_TYPE_KEY
argument_list|,
name|NettyTransport
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"node.mode"
argument_list|,
literal|"network"
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.profiles.client1.port"
argument_list|,
name|randomPortRange
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.profiles.client1.reuse_address"
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
annotation|@
name|Network
annotation|@
name|TestLogging
argument_list|(
literal|"transport.netty:DEBUG"
argument_list|)
DECL|method|testThatTransportClientCanConnect
specifier|public
name|void
name|testThatTransportClientCanConnect
parameter_list|()
throws|throws
name|Exception
block|{
name|Settings
name|settings
init|=
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.name"
argument_list|,
name|internalCluster
argument_list|()
operator|.
name|getClusterName
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|TransportModule
operator|.
name|TRANSPORT_TYPE_KEY
argument_list|,
name|NettyTransport
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
try|try
init|(
name|TransportClient
name|transportClient
init|=
operator|new
name|TransportClient
argument_list|(
name|settings
argument_list|)
init|)
block|{
name|transportClient
operator|.
name|addTransportAddress
argument_list|(
operator|new
name|InetSocketTransportAddress
argument_list|(
literal|"localhost"
argument_list|,
name|randomPort
argument_list|)
argument_list|)
expr_stmt|;
name|ClusterHealthResponse
name|response
init|=
name|transportClient
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getStatus
argument_list|()
argument_list|,
name|is
argument_list|(
name|ClusterHealthStatus
operator|.
name|GREEN
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

