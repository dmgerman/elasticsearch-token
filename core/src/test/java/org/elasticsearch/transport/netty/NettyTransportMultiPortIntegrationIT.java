begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport.netty
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|netty
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|LuceneTestCase
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
name|ESIntegTestCase
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
name|transport
operator|.
name|TransportModule
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
name|Settings
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
name|ESIntegTestCase
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
name|ESIntegTestCase
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
name|*
import|;
end_import

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
name|numClientNodes
operator|=
literal|0
argument_list|)
comment|//@LuceneTestCase.AwaitsFix(bugUrl = "https://github.com/elastic/elasticsearch/issues/12788")
DECL|class|NettyTransportMultiPortIntegrationIT
specifier|public
class|class
name|NettyTransportMultiPortIntegrationIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|randomPort
specifier|private
specifier|static
name|int
name|randomPort
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|randomPortRange
specifier|private
specifier|static
name|String
name|randomPortRange
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
if|if
condition|(
name|randomPort
operator|==
operator|-
literal|1
condition|)
block|{
name|randomPort
operator|=
name|randomIntBetween
argument_list|(
literal|49152
argument_list|,
literal|65525
argument_list|)
expr_stmt|;
name|randomPortRange
operator|=
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
expr_stmt|;
block|}
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
literal|"netty"
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
literal|"node.local"
argument_list|,
literal|false
argument_list|)
comment|// ensure randomization doesn't set local mode, since this has higher precedence
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
literal|"transport.profiles.client1.publish_host"
argument_list|,
literal|"127.0.0.7"
argument_list|)
operator|.
name|put
argument_list|(
literal|"transport.profiles.client1.publish_port"
argument_list|,
literal|"4321"
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
literal|"netty"
argument_list|)
operator|.
name|put
argument_list|(
literal|"path.home"
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
decl_stmt|;
try|try
init|(
name|TransportClient
name|transportClient
init|=
name|TransportClient
operator|.
name|builder
argument_list|()
operator|.
name|settings
argument_list|(
name|settings
argument_list|)
operator|.
name|loadConfigSettings
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
init|)
block|{
name|transportClient
operator|.
name|addTransportAddress
argument_list|(
operator|new
name|InetSocketTransportAddress
argument_list|(
literal|"127.0.0.1"
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
annotation|@
name|Test
annotation|@
name|Network
DECL|method|testThatInfosAreExposed
specifier|public
name|void
name|testThatInfosAreExposed
parameter_list|()
throws|throws
name|Exception
block|{
name|NodesInfoResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareNodesInfo
argument_list|()
operator|.
name|clear
argument_list|()
operator|.
name|setTransport
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
for|for
control|(
name|NodeInfo
name|nodeInfo
range|:
name|response
operator|.
name|getNodes
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
name|nodeInfo
operator|.
name|getTransport
argument_list|()
operator|.
name|getProfileAddresses
argument_list|()
operator|.
name|keySet
argument_list|()
argument_list|,
name|hasSize
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nodeInfo
operator|.
name|getTransport
argument_list|()
operator|.
name|getProfileAddresses
argument_list|()
argument_list|,
name|hasKey
argument_list|(
literal|"client1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|nodeInfo
operator|.
name|getTransport
argument_list|()
operator|.
name|getProfileAddresses
argument_list|()
operator|.
name|get
argument_list|(
literal|"client1"
argument_list|)
operator|.
name|boundAddress
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|InetSocketTransportAddress
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// bound address
name|InetSocketTransportAddress
name|inetSocketTransportAddress
init|=
operator|(
name|InetSocketTransportAddress
operator|)
name|nodeInfo
operator|.
name|getTransport
argument_list|()
operator|.
name|getProfileAddresses
argument_list|()
operator|.
name|get
argument_list|(
literal|"client1"
argument_list|)
operator|.
name|boundAddress
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|inetSocketTransportAddress
operator|.
name|address
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|,
name|is
argument_list|(
name|allOf
argument_list|(
name|greaterThanOrEqualTo
argument_list|(
name|randomPort
argument_list|)
argument_list|,
name|lessThanOrEqualTo
argument_list|(
name|randomPort
operator|+
literal|10
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// publish address
name|assertThat
argument_list|(
name|nodeInfo
operator|.
name|getTransport
argument_list|()
operator|.
name|getProfileAddresses
argument_list|()
operator|.
name|get
argument_list|(
literal|"client1"
argument_list|)
operator|.
name|publishAddress
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|InetSocketTransportAddress
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|InetSocketTransportAddress
name|publishAddress
init|=
operator|(
name|InetSocketTransportAddress
operator|)
name|nodeInfo
operator|.
name|getTransport
argument_list|()
operator|.
name|getProfileAddresses
argument_list|()
operator|.
name|get
argument_list|(
literal|"client1"
argument_list|)
operator|.
name|publishAddress
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|publishAddress
operator|.
name|address
argument_list|()
operator|.
name|getHostName
argument_list|()
argument_list|,
name|is
argument_list|(
literal|"127.0.0.7"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|publishAddress
operator|.
name|address
argument_list|()
operator|.
name|getPort
argument_list|()
argument_list|,
name|is
argument_list|(
literal|4321
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

